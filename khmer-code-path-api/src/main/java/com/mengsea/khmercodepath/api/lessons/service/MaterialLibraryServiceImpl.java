package com.mengsea.khmercodepath.api.lessons.service;

import com.mengsea.khmercodepath.api.lessons.payload.AssignLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.lessons.payload.MaterialLibraryItemPayload;
import com.mengsea.khmercodepath.api.lessons.storage.LocalUploadStorage;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryItem;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryMaterial;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryItemRepository;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryMaterialRepository;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialLibraryServiceImpl implements MaterialLibraryService {

    private final MaterialLibraryItemRepository materialLibraryItemRepository;
    private final MaterialLibraryMaterialRepository materialLibraryMaterialRepository;
    private final LessonManagementService lessonManagementService;
    private final ClassAccessHelper classAccessHelper;
    private final LocalUploadStorage localUploadStorage;

    @Override
    @Transactional(readOnly = true)
    public List<MaterialLibraryItemPayload> listLibrary(String search, String moduleTag) {
        User me = SecurityUtils.requireCurrentUser();
        String module = blankToNull(moduleTag);
        if (module != null && module.equalsIgnoreCase("All Modules")) {
            module = null;
        }
        return materialLibraryItemRepository.searchByTeacher(me.getUuid(), blankToNull(search), module)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    @Override
    @Transactional
    public MaterialLibraryItemPayload createLibraryItem(CreateLibraryItemRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem entity = new MaterialLibraryItem();
        entity.setTeacher(me);
        entity.setTitle(request.getTitle().trim());
        entity.setModuleTag(blankToNull(request.getModuleTag()));
        entity.setDescription(blankToNull(request.getDescription()));
        entity.setIconType(request.getIconType() != null ? request.getIconType() : LibraryIconType.SLIDES);
        entity.setGradient(
                request.getGradient() != null && !request.getGradient().isBlank()
                        ? request.getGradient().trim()
                        : "from-violet-800 to-violet-600"
        );
        entity.setDeleted(false);
        materialLibraryItemRepository.save(entity);
        return toPayload(entity);
    }

    @Override
    @Transactional
    public void uploadLibraryMaterials(Long libraryItemId, List<MultipartFile> files) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = materialLibraryItemRepository
                .findByIdAndTeacher_UuidAndDeletedFalse(libraryItemId, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND));
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        long existing = materialLibraryMaterialRepository.countByLibraryItem_IdAndDeletedFalse(libraryItemId);
        if (existing + files.size() > 10) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        for (MultipartFile file : files) {
            LocalUploadStorage.StoredFile stored =
                    localUploadStorage.store("library", libraryItemId, file);
            MaterialLibraryMaterial material = new MaterialLibraryMaterial();
            material.setLibraryItem(item);
            material.setFileName(stored.fileName());
            material.setContentType(stored.contentType());
            material.setFileSizeBytes(stored.sizeBytes());
            material.setStorageKey(stored.storageKey());
            material.setDeleted(false);
            materialLibraryMaterialRepository.save(material);
        }
    }

    @Override
    @Transactional
    public LessonDetailPayload assignToClass(Long libraryItemId, AssignLibraryItemRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = materialLibraryItemRepository
                .findByIdAndTeacher_UuidAndDeletedFalse(libraryItemId, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND));
        LmsClass target = classAccessHelper.requireReadableClass(request.getTargetClassId());
        classAccessHelper.assertCanManageClass(target);
        CreateLessonRequest create = new CreateLessonRequest();
        create.setClassId(request.getTargetClassId());
        create.setTitle(item.getTitle());
        create.setDescription(item.getDescription());
        create.setModuleTag(item.getModuleTag());
        create.setLibraryItemId(item.getId());
        return lessonManagementService.createLesson(create);
    }

    private MaterialLibraryItemPayload toPayload(MaterialLibraryItem item) {
        long assetCount = materialLibraryMaterialRepository.countByLibraryItem_IdAndDeletedFalse(item.getId());
        return MaterialLibraryItemPayload.builder()
                .id(item.getId())
                .title(item.getTitle())
                .moduleTag(item.getModuleTag())
                .description(item.getDescription())
                .iconType(item.getIconType())
                .gradient(item.getGradient())
                .assetCount(assetCount)
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
