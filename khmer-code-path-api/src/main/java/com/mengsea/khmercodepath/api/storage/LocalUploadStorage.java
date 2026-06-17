package com.mengsea.khmercodepath.api.storage;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * In-memory test profile only. Runtime uploads use MinIO ({@link MinioUploadStorage}).
 */
@Component
@Profile("test")
@ConditionalOnProperty(name = "lms.storage.provider", havingValue = "local")
public class LocalUploadStorage implements UploadStorage {

    private static final int MAX_FILES_PER_BATCH = 10;

    private final Path root;

    public LocalUploadStorage(@Value("${lms.uploads-dir:./build/test-uploads}") String uploadsDir)
            throws IOException {
        this.root = Path.of(uploadsDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    @Override
    public int maxFilesPerBatch() {
        return MAX_FILES_PER_BATCH;
    }

    @Override
    public StoredFile store(String category, Long ownerId, MultipartFile file) {
        MaterialUploadValidator.validateFile(file);
        String safeName = MaterialUploadValidator.sanitizeFileName(file.getOriginalFilename());
        String key = category + "/" + ownerId + "/" + UUID.randomUUID() + "_" + safeName;
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
        return new StoredFile(key, safeName, file.getContentType(), file.getSize());
    }

    @Override
    public StoredFile storeAvatar(String userId, MultipartFile file) {
        AvatarUploadValidator.validateFile(file);
        String safeName = AvatarUploadValidator.sanitizeFileName(file.getOriginalFilename());
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + "_" + safeName;
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
        return new StoredFile(key, safeName, file.getContentType(), file.getSize());
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        try {
            Path file = root.resolve(storageKey).normalize();
            if (!file.startsWith(root) || !Files.exists(file)) {
                throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
            }
            return resource;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public InputStream openStream(String storageKey) {
        try {
            Path file = root.resolve(storageKey).normalize();
            if (!file.startsWith(root) || !Files.exists(file)) {
                throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
            }
            return Files.newInputStream(file);
        } catch (IOException ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void copyStorageFile(String sourceKey, String targetKey) {
        try {
            Path source = root.resolve(sourceKey).normalize();
            Path target = root.resolve(targetKey).normalize();
            if (!source.startsWith(root) || !target.startsWith(root) || !Files.exists(source)) {
                return;
            }
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path file = root.resolve(storageKey).normalize();
            if (file.startsWith(root) && Files.exists(file)) {
                Files.delete(file);
            }
        } catch (IOException ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }
}
