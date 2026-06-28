package com.mengsea.khmercodepath.api.lessons.service;

import com.mengsea.khmercodepath.api.lessons.payload.AssignLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LinkLibraryMaterialsRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LibraryMaterialPayload;
import com.mengsea.khmercodepath.api.lessons.config.MaterialLibraryProperties;
import com.mengsea.khmercodepath.api.lessons.payload.MaterialLibraryItemPayload;
import com.mengsea.khmercodepath.api.lessons.payload.UpdateLibraryItemRequest;
import com.mengsea.khmercodepath.commons.constant.MaterialSourceType;
import com.mengsea.khmercodepath.commons.constant.RagIndexStatus;
import com.mengsea.khmercodepath.commons.repository.MaterialRagIndexRepository;
import com.mengsea.khmercodepath.api.ai.rag.MaterialRagVectorService;
import com.mengsea.khmercodepath.api.storage.UploadStorage;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialLibraryServiceImpl implements MaterialLibraryService {

    private static final String FILE_POOL_LABEL = "Stored files";

    private final MaterialLibraryItemRepository materialLibraryItemRepository;
    private final MaterialLibraryMaterialRepository materialLibraryMaterialRepository;
    private final LessonManagementService lessonManagementService;
    private final ClassAccessHelper classAccessHelper;
    private final UploadStorage uploadStorage;
    private final MaterialRagVectorService materialRagVectorService;
    private final MaterialRagIndexRepository materialRagIndexRepository;
    private final MaterialLibraryProperties materialLibraryProperties;

    @Override
    @Transactional
    public List<LibraryMaterialPayload> listPoolFiles(String search) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem pool = getOrCreateFilePool(me);
        String q = blankToNull(search);
        return materialLibraryMaterialRepository
                .findByLibraryItem_IdAndDeletedFalseOrderByCreatedAtAsc(pool.getId())
                .stream()
                .filter(m -> q == null || m.getFileName().toLowerCase().contains(q.toLowerCase()))
                .map(m -> toLibraryMaterialPayload(m, true))
                .toList();
    }

    @Override
    @Transactional
    public void uploadPoolFiles(List<MultipartFile> files) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem pool = getOrCreateFilePool(me);
        uploadMaterialsToItem(pool, files, materialLibraryProperties.getFilePool().getMaxFiles());
    }

    @Override
    @Transactional
    public void deletePoolFile(Long materialId) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem pool = getOrCreateFilePool(me);
        MaterialLibraryMaterial material = materialLibraryMaterialRepository
                .findByIdAndDeletedFalse(materialId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (!material.getLibraryItem().getId().equals(pool.getId())) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }
        softDeleteLibraryMaterial(material);
    }

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
    @Transactional(readOnly = true)
    public MaterialLibraryItemPayload getLibraryItem(Long libraryItemId) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = requireLessonTemplate(libraryItemId, me);
        return toPayload(item);
    }

    @Override
    @Transactional
    public MaterialLibraryItemPayload updateLibraryItem(Long libraryItemId, UpdateLibraryItemRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = requireLessonTemplate(libraryItemId, me);
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            item.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            item.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getModuleTag() != null) {
            item.setModuleTag(blankToNull(request.getModuleTag()));
        }
        if (request.getIconType() != null) {
            item.setIconType(request.getIconType());
        }
        if (request.getGradient() != null && !request.getGradient().isBlank()) {
            item.setGradient(request.getGradient().trim());
        }
        materialLibraryItemRepository.save(item);
        return toPayload(item);
    }

    @Override
    @Transactional
    public void uploadLibraryMaterials(Long libraryItemId, List<MultipartFile> files) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = requireLessonTemplate(libraryItemId, me);
        uploadMaterialsToItem(item, files, 10);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryMaterialPayload> listLibraryMaterials(Long libraryItemId) {
        User me = SecurityUtils.requireCurrentUser();
        requireLessonTemplate(libraryItemId, me);
        return materialLibraryMaterialRepository
                .findByLibraryItem_IdAndDeletedFalseOrderByCreatedAtAsc(libraryItemId)
                .stream()
                .map(m -> toLibraryMaterialPayload(m, false))
                .toList();
    }

    @Override
    @Transactional
    public LessonDetailPayload assignToClass(Long libraryItemId, AssignLibraryItemRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = requireLessonTemplate(libraryItemId, me);
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

    @Override
    @Transactional
    public void deleteLibraryItem(Long libraryItemId) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem item = requireLessonTemplate(libraryItemId, me);
        for (MaterialLibraryMaterial material :
                materialLibraryMaterialRepository.findByLibraryItem_IdAndDeletedFalseOrderByCreatedAtAsc(
                        libraryItemId)) {
            softDeleteLibraryMaterial(material);
        }
        item.setDeleted(true);
        materialLibraryItemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteLibraryMaterial(Long libraryItemId, Long materialId) {
        User me = SecurityUtils.requireCurrentUser();
        requireLessonTemplate(libraryItemId, me);
        MaterialLibraryMaterial material = materialLibraryMaterialRepository
                .findByIdAndDeletedFalse(materialId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (!material.getLibraryItem().getId().equals(libraryItemId)) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }
        softDeleteLibraryMaterial(material);
    }

    @Override
    @Transactional
    public List<LibraryMaterialPayload> linkLibraryMaterials(
            Long targetLibraryItemId,
            LinkLibraryMaterialsRequest request
    ) {
        User me = SecurityUtils.requireCurrentUser();
        MaterialLibraryItem target = requireLessonTemplate(targetLibraryItemId, me);
        long existing = materialLibraryMaterialRepository.countByLibraryItem_IdAndDeletedFalse(targetLibraryItemId);
        if (existing + request.getSourceMaterialIds().size() > 10) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        List<LibraryMaterialPayload> linked = new ArrayList<>();
        for (Long sourceMaterialId : request.getSourceMaterialIds()) {
            if (sourceMaterialId == null) {
                continue;
            }
            MaterialLibraryMaterial source = materialLibraryMaterialRepository
                    .findByIdAndDeletedFalse(sourceMaterialId)
                    .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
            MaterialLibraryItem sourceItem = source.getLibraryItem();
            if (!Objects.equals(sourceItem.getTeacher().getUuid(), me.getUuid()) || sourceItem.isDeleted()) {
                throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
            }
            if (Objects.equals(sourceItem.getId(), target.getId())) {
                linked.add(toLibraryMaterialPayload(source, sourceItem.isFilePool()));
                continue;
            }
            boolean alreadyLinked = materialLibraryMaterialRepository
                    .findByLibraryItem_IdAndDeletedFalseOrderByCreatedAtAsc(targetLibraryItemId)
                    .stream()
                    .anyMatch(m -> m.getFileName().equals(source.getFileName())
                            && Objects.equals(m.getFileSizeBytes(), source.getFileSizeBytes()));
            if (alreadyLinked) {
                continue;
            }
            String newKey = "library/" + targetLibraryItemId + "/"
                    + UUID.randomUUID() + "_" + source.getFileName();
            uploadStorage.copyStorageFile(source.getStorageKey(), newKey);
            MaterialLibraryMaterial copy = new MaterialLibraryMaterial();
            copy.setLibraryItem(target);
            copy.setFileName(source.getFileName());
            copy.setContentType(source.getContentType());
            copy.setFileSizeBytes(source.getFileSizeBytes());
            copy.setStorageKey(newKey);
            copy.setDeleted(false);
            materialLibraryMaterialRepository.save(copy);
            try {
                materialRagVectorService.registerLibraryMaterial(
                        copy.getId(), newKey, copy.getFileName(), copy.getContentType());
            } catch (Exception ex) {
                log.warn("Linked library material {} but RAG index registration failed: {}",
                        copy.getId(), ex.getMessage());
            }
            linked.add(toLibraryMaterialPayload(copy, false));
        }
        return linked;
    }

    private MaterialLibraryItem requireLessonTemplate(Long libraryItemId, User me) {
        MaterialLibraryItem item = materialLibraryItemRepository
                .findByIdAndTeacher_UuidAndDeletedFalse(libraryItemId, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND));
        if (item.isFilePool()) {
            throw new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND);
        }
        return item;
    }

    private MaterialLibraryItem getOrCreateFilePool(User teacher) {
        return materialLibraryItemRepository
                .findByTeacher_UuidAndFilePoolTrueAndDeletedFalse(teacher.getUuid())
                .orElseGet(() -> createFilePool(teacher));
    }

    private MaterialLibraryItem createFilePool(User teacher) {
        MaterialLibraryItem pool = new MaterialLibraryItem();
        pool.setTeacher(teacher);
        pool.setFilePool(true);
        pool.setTitle(FILE_POOL_LABEL);
        pool.setIconType(LibraryIconType.SLIDES);
        pool.setGradient("from-slate-700 to-slate-500");
        pool.setDeleted(false);
        return materialLibraryItemRepository.save(pool);
    }

    private void uploadMaterialsToItem(
            MaterialLibraryItem item,
            List<MultipartFile> files,
            int maxFilesPerItem
    ) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        if (files.size() > uploadStorage.maxFilesPerBatch()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        long existing = materialLibraryMaterialRepository.countByLibraryItem_IdAndDeletedFalse(item.getId());
        if (existing + files.size() > maxFilesPerItem) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        for (MultipartFile file : files) {
            UploadStorage.StoredFile stored =
                    uploadStorage.store("library", item.getId(), file);
            MaterialLibraryMaterial material = new MaterialLibraryMaterial();
            material.setLibraryItem(item);
            material.setFileName(stored.fileName());
            material.setContentType(stored.contentType());
            material.setFileSizeBytes(stored.sizeBytes());
            material.setStorageKey(stored.storageKey());
            material.setDeleted(false);
            materialLibraryMaterialRepository.save(material);
            try {
                materialRagVectorService.registerLibraryMaterial(
                        material.getId(), stored.storageKey(), stored.fileName(), stored.contentType());
            } catch (Exception ex) {
                log.warn("Saved library material {} but RAG index registration failed: {}",
                        material.getId(), ex.getMessage());
            }
        }
    }

    private void softDeleteLibraryMaterial(MaterialLibraryMaterial material) {
        material.setDeleted(true);
        materialLibraryMaterialRepository.save(material);
        materialRagVectorService.removeIndex(
                MaterialSourceType.LIBRARY_MATERIAL, material.getId(), material.getStorageKey());
    }

    private MaterialLibraryItemPayload toPayload(MaterialLibraryItem item) {
        List<LibraryMaterialPayload> materials = materialLibraryMaterialRepository
                .findByLibraryItem_IdAndDeletedFalseOrderByCreatedAtAsc(item.getId())
                .stream()
                .map(m -> toLibraryMaterialPayload(m, item.isFilePool()))
                .toList();
        return MaterialLibraryItemPayload.builder()
                .id(item.getId())
                .title(item.getTitle())
                .moduleTag(item.getModuleTag())
                .description(item.getDescription())
                .iconType(item.getIconType())
                .gradient(item.getGradient())
                .assetCount(materials.size())
                .materials(materials)
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private LibraryMaterialPayload toLibraryMaterialPayload(
            MaterialLibraryMaterial material,
            boolean poolFile
    ) {
        String ragStatus = materialRagIndexRepository
                .findBySourceTypeAndSourceId(MaterialSourceType.LIBRARY_MATERIAL, material.getId())
                .map(idx -> idx.getStatus().name())
                .orElse(RagIndexStatus.NOT_INDEXED.name());
        return LibraryMaterialPayload.builder()
                .id(material.getId())
                .libraryItemId(material.getLibraryItem().getId())
                .fileName(material.getFileName())
                .contentType(material.getContentType())
                .fileSizeBytes(material.getFileSizeBytes())
                .ragStatus(ragStatus)
                .poolFile(poolFile)
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
