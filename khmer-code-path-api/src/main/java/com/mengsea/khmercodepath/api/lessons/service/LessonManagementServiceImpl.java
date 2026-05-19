package com.mengsea.khmercodepath.api.lessons.service;

import com.mengsea.khmercodepath.api.lessons.payload.CopyLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LessonMaterialPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LessonSummaryPayload;
import com.mengsea.khmercodepath.api.lessons.payload.UpdateLessonRequest;
import com.mengsea.khmercodepath.api.lessons.storage.LocalUploadStorage;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.Lesson;
import com.mengsea.khmercodepath.commons.domain.LessonMaterial;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryItem;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryMaterial;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.LessonMaterialRepository;
import com.mengsea.khmercodepath.commons.repository.LessonRepository;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryItemRepository;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryMaterialRepository;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import com.mengsea.khmercodepath.api.notifications.service.NotificationPublisher;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LessonManagementServiceImpl implements LessonManagementService {

    private final LessonRepository lessonRepository;
    private final LessonMaterialRepository lessonMaterialRepository;
    private final MaterialLibraryItemRepository materialLibraryItemRepository;
    private final MaterialLibraryMaterialRepository materialLibraryMaterialRepository;
    private final ClassAccessHelper classAccessHelper;
    private final LocalUploadStorage localUploadStorage;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<LessonSummaryPayload> listLessons(Long classId, String teacherId) {
        if (classId == null) {
            return List.of();
        }
        classAccessHelper.requireReadableClass(classId);
        return lessonRepository.findByLmsClass_IdAndDeletedFalseOrderBySortOrderAscCreatedAtAsc(classId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDetailPayload getLesson(Long id) {
        Lesson lesson = requireReadableLesson(id);
        return toDetail(lesson);
    }

    @Override
    @Transactional
    public LessonDetailPayload createLesson(CreateLessonRequest request) {
        LmsClass lmsClass = classAccessHelper.requireReadableClass(request.getClassId());
        classAccessHelper.assertCanManageClass(lmsClass);
        Lesson entity = new Lesson();
        entity.setLmsClass(lmsClass);
        entity.setTitle(request.getTitle().trim());
        entity.setDescription(blankToNull(request.getDescription()));
        entity.setModuleTag(blankToNull(request.getModuleTag()));
        entity.setSortOrder((int) lessonRepository.countByLmsClass_IdAndDeletedFalse(lmsClass.getId()));
        entity.setDeleted(false);
        if (request.getLibraryItemId() != null) {
            applyLibraryTemplate(entity, request.getLibraryItemId());
        }
        lessonRepository.save(entity);
        if (request.getLibraryItemId() != null) {
            copyLibraryMaterialsToLesson(request.getLibraryItemId(), entity);
        }
        User me = SecurityUtils.requireCurrentUser();
        notificationPublisher.onLessonPublished(lmsClass, entity.getTitle(), entity.getId(), me.getUuid());
        return toDetail(entity);
    }

    @Override
    @Transactional
    public LessonDetailPayload updateLesson(Long id, UpdateLessonRequest request) {
        Lesson entity = requireReadableLesson(id);
        classAccessHelper.assertCanManageClass(entity.getLmsClass());
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getSummary() != null) {
            entity.setSummary(blankToNull(request.getSummary()));
        }
        if (request.getModuleTag() != null) {
            entity.setModuleTag(blankToNull(request.getModuleTag()));
        }
        lessonRepository.save(entity);
        return toDetail(entity);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson entity = requireReadableLesson(id);
        classAccessHelper.assertCanManageClass(entity.getLmsClass());
        entity.setDeleted(true);
        lessonRepository.save(entity);
    }

    @Override
    @Transactional
    public List<LessonMaterialPayload> uploadMaterials(Long lessonId, List<MultipartFile> files) {
        Lesson lesson = requireReadableLesson(lessonId);
        classAccessHelper.assertCanManageClass(lesson.getLmsClass());
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        if (files.size() > localUploadStorage.maxFilesPerBatch()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        long existing = lessonMaterialRepository.countByLesson_IdAndDeletedFalse(lessonId);
        if (existing + files.size() > 10) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        List<LessonMaterialPayload> result = new ArrayList<>();
        for (MultipartFile file : files) {
            LocalUploadStorage.StoredFile stored =
                    localUploadStorage.store("lessons", lessonId, file);
            LessonMaterial material = new LessonMaterial();
            material.setLesson(lesson);
            material.setFileName(stored.fileName());
            material.setContentType(stored.contentType());
            material.setFileSizeBytes(stored.sizeBytes());
            material.setStorageKey(stored.storageKey());
            material.setDeleted(false);
            lessonMaterialRepository.save(material);
            result.add(toMaterialPayload(material));
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteMaterial(Long lessonId, Long materialId) {
        Lesson lesson = requireReadableLesson(lessonId);
        classAccessHelper.assertCanManageClass(lesson.getLmsClass());
        LessonMaterial material = lessonMaterialRepository.findByIdAndDeletedFalse(materialId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (!Objects.equals(material.getLesson().getId(), lessonId)) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }
        material.setDeleted(true);
        lessonMaterialRepository.save(material);
    }

    @Override
    @Transactional
    public LessonDetailPayload copyLesson(Long id, CopyLessonRequest request) {
        Lesson source = requireReadableLesson(id);
        classAccessHelper.assertCanManageClass(source.getLmsClass());
        LmsClass target = classAccessHelper.requireReadableClass(request.getTargetClassId());
        classAccessHelper.assertCanManageClass(target);
        Lesson copy = new Lesson();
        copy.setLmsClass(target);
        copy.setTitle(source.getTitle());
        copy.setDescription(source.getDescription());
        copy.setSummary(source.getSummary());
        copy.setModuleTag(source.getModuleTag());
        copy.setLibraryItemId(source.getLibraryItemId());
        copy.setSortOrder((int) lessonRepository.countByLmsClass_IdAndDeletedFalse(target.getId()));
        copy.setDeleted(false);
        lessonRepository.save(copy);
        if (request.isIncludeMaterials()) {
            for (LessonMaterial src : lessonMaterialRepository.findByLesson_IdAndDeletedFalseOrderByCreatedAtAsc(id)) {
                String newKey = "lessons/" + copy.getId() + "/" + java.util.UUID.randomUUID() + "_" + src.getFileName();
                localUploadStorage.copyStorageFile(src.getStorageKey(), newKey);
                LessonMaterial material = new LessonMaterial();
                material.setLesson(copy);
                material.setFileName(src.getFileName());
                material.setContentType(src.getContentType());
                material.setFileSizeBytes(src.getFileSizeBytes());
                material.setStorageKey(newKey);
                material.setDeleted(false);
                lessonMaterialRepository.save(material);
            }
        }
        return toDetail(copy);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadMaterial(Long lessonId, Long materialId) {
        requireReadableLesson(lessonId);
        LessonMaterial material = lessonMaterialRepository.findByIdAndDeletedFalse(materialId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (!Objects.equals(material.getLesson().getId(), lessonId)) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }
        return localUploadStorage.loadAsResource(material.getStorageKey());
    }

    private Lesson requireReadableLesson(Long id) {
        Lesson lesson = lessonRepository.findByIdAndDeletedFalseWithClass(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.LESSON_NOT_FOUND));
        classAccessHelper.assertCanRead(lesson.getLmsClass());
        return lesson;
    }

    private void applyLibraryTemplate(Lesson entity, Long libraryItemId) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = materialLibraryItemRepository
                .findByIdAndTeacher_UuidAndDeletedFalse(libraryItemId, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND));
        if (entity.getDescription() == null) {
            entity.setDescription(item.getDescription());
        }
        if (entity.getModuleTag() == null) {
            entity.setModuleTag(item.getModuleTag());
        }
        entity.setLibraryItemId(item.getId());
    }

    private void copyLibraryMaterialsToLesson(Long libraryItemId, Lesson lesson) {
        for (MaterialLibraryMaterial src :
                materialLibraryMaterialRepository.findByLibraryItem_IdAndDeletedFalseOrderByCreatedAtAsc(
                        libraryItemId)) {
            String newKey = "lessons/" + lesson.getId() + "/" + java.util.UUID.randomUUID() + "_" + src.getFileName();
            localUploadStorage.copyStorageFile(src.getStorageKey(), newKey);
            LessonMaterial material = new LessonMaterial();
            material.setLesson(lesson);
            material.setFileName(src.getFileName());
            material.setContentType(src.getContentType());
            material.setFileSizeBytes(src.getFileSizeBytes());
            material.setStorageKey(newKey);
            material.setDeleted(false);
            lessonMaterialRepository.save(material);
        }
    }

    private LessonSummaryPayload toSummary(Lesson lesson) {
        long materialCount = lessonMaterialRepository.countByLesson_IdAndDeletedFalse(lesson.getId());
        return LessonSummaryPayload.builder()
                .id(lesson.getId())
                .classId(lesson.getLmsClass().getId())
                .className(lesson.getLmsClass().getName())
                .title(lesson.getTitle())
                .moduleTag(lesson.getModuleTag())
                .materialCount(materialCount)
                .aiReady(materialCount > 0)
                .createdAt(lesson.getCreatedAt())
                .build();
    }

    private LessonDetailPayload toDetail(Lesson lesson) {
        long materialCount = lessonMaterialRepository.countByLesson_IdAndDeletedFalse(lesson.getId());
        List<LessonMaterialPayload> materials = lessonMaterialRepository
                .findByLesson_IdAndDeletedFalseOrderByCreatedAtAsc(lesson.getId())
                .stream()
                .map(this::toMaterialPayload)
                .toList();
        return LessonDetailPayload.builder()
                .id(lesson.getId())
                .classId(lesson.getLmsClass().getId())
                .className(lesson.getLmsClass().getName())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .summary(lesson.getSummary())
                .moduleTag(lesson.getModuleTag())
                .aiReady(materialCount > 0)
                .materialsProcessing(false)
                .materials(materials)
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    private LessonMaterialPayload toMaterialPayload(LessonMaterial material) {
        return LessonMaterialPayload.builder()
                .id(material.getId())
                .fileName(material.getFileName())
                .contentType(material.getContentType())
                .fileSizeBytes(material.getFileSizeBytes())
                .downloadUrl("/api/v1/lessons/" + material.getLesson().getId()
                        + "/materials/" + material.getId() + "/download")
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
