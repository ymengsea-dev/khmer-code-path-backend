package com.mengsea.khmercodepath.api.schools.service;

import com.mengsea.khmercodepath.api.storage.SchoolCoverUploadValidator;
import com.mengsea.khmercodepath.api.storage.UploadStorage;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.SchoolRepository;
import com.mengsea.khmercodepath.commons.service.SchoolRegistrationSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SchoolCoverService {

    private final SchoolRepository schoolRepository;
    private final SchoolRegistrationSupport schoolRegistrationSupport;
    private final UploadStorage uploadStorage;

    @Transactional
    public School uploadCover(School school, MultipartFile file) {
        String previousKey = school.getCoverStorageKey();
        UploadStorage.StoredFile stored = uploadStorage.storeSchoolCover(school.getId(), file);
        school.setCoverStorageKey(stored.storageKey());
        schoolRepository.save(school);
        deleteStorageKeyBestEffort(previousKey, stored.storageKey());
        return school;
    }

    @Transactional
    public School removeCover(School school) {
        String previousKey = school.getCoverStorageKey();
        school.setCoverStorageKey(null);
        schoolRepository.save(school);
        deleteStorageKeyBestEffort(previousKey, null);
        return school;
    }

    public CoverResource getCoverBySlug(String slug) {
        School school = schoolRegistrationSupport.requireRegistrationSchool(slug);
        return loadCover(school);
    }

    private CoverResource loadCover(School school) {
        String storageKey = school.getCoverStorageKey();
        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        Resource resource = uploadStorage.loadAsResource(storageKey);
        return new CoverResource(resource, contentTypeForKey(storageKey));
    }

    private void deleteStorageKeyBestEffort(String previousKey, String currentKey) {
        if (previousKey != null && !previousKey.isBlank() && !previousKey.equals(currentKey)) {
            try {
                uploadStorage.delete(previousKey);
            } catch (RuntimeException ignored) {
                /* best-effort cleanup */
            }
        }
    }

    private static MediaType contentTypeForKey(String key) {
        return switch (SchoolCoverUploadValidator.extension(key)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    public record CoverResource(Resource resource, MediaType contentType) {}
}
