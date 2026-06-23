package com.mengsea.khmercodepath.api.storage;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/** Validation for school cover images (JPG/PNG/WebP, max 10MB). */
public final class SchoolCoverUploadValidator {

    public static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private SchoolCoverUploadValidator() {}

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
            return "cover";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
