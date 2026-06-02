package com.mengsea.khmercodepath.api.storage;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/**
 * Shared validation for lesson / library material uploads (used by MinIO and test-local storage).
 */
public final class MaterialUploadValidator {

    public static final long MAX_FILE_BYTES = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "pptx", "docx", "ppt", "doc");

    private MaterialUploadValidator() {}

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new BusinessException(ExceptionCode.FILE_TOO_LARGE);
        }
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        if (!ALLOWED_EXT.contains(extension(name))) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    public static String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public static boolean isAllowedExtension(String fileName) {
        return ALLOWED_EXT.contains(extension(fileName));
    }
}
