package com.mengsea.khmercodepath.api.lessons.storage;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalUploadStorage {

    private static final long MAX_FILE_BYTES = 50L * 1024 * 1024;
    private static final int MAX_FILES_PER_BATCH = 10;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "pptx", "docx", "ppt", "doc");

    private final Path root;

    public LocalUploadStorage(@Value("${lms.uploads-dir:./uploads}") String uploadsDir) throws IOException {
        this.root = Path.of(uploadsDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public int maxFilesPerBatch() {
        return MAX_FILES_PER_BATCH;
    }

    public StoredFile store(String category, Long ownerId, MultipartFile file) {
        validateFile(file);
        String safeName = sanitizeFileName(file.getOriginalFilename());
        String key = category + "/" + ownerId + "/" + UUID.randomUUID() + "_" + safeName;
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
        return new StoredFile(key, safeName, file.getContentType(), file.getSize());
    }

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

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        String ext = extension(name);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record StoredFile(String storageKey, String fileName, String contentType, long sizeBytes) {}
}
