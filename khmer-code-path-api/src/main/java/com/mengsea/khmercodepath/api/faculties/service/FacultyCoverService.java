package com.mengsea.khmercodepath.api.faculties.service;

import com.mengsea.khmercodepath.api.storage.SchoolCoverUploadValidator;
import com.mengsea.khmercodepath.api.storage.UploadStorage;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.Faculty;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FacultyCoverService {

    private final FacultyRepository facultyRepository;
    private final UploadStorage uploadStorage;

    @Transactional
    public Faculty uploadCover(School school, Long facultyId, MultipartFile file) {
        Faculty faculty = requireFaculty(school, facultyId);
        String previousKey = faculty.getCoverStorageKey();
        UploadStorage.StoredFile stored = uploadStorage.storeFacultyCover(faculty.getId(), file);
        faculty.setCoverStorageKey(stored.storageKey());
        facultyRepository.save(faculty);
        deleteStorageKeyBestEffort(previousKey, stored.storageKey());
        return faculty;
    }

    @Transactional
    public Faculty removeCover(School school, Long facultyId) {
        Faculty faculty = requireFaculty(school, facultyId);
        String previousKey = faculty.getCoverStorageKey();
        faculty.setCoverStorageKey(null);
        facultyRepository.save(faculty);
        deleteStorageKeyBestEffort(previousKey, null);
        return faculty;
    }

    public CoverResource getCover(School school, Long facultyId) {
        Faculty faculty = requireFaculty(school, facultyId);
        return loadCover(faculty);
    }

    private Faculty requireFaculty(School school, Long facultyId) {
        return facultyRepository.findByIdAndSchool_IdAndDeletedFalse(facultyId, school.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FACULTY_NOT_FOUND));
    }

    private CoverResource loadCover(Faculty faculty) {
        String storageKey = faculty.getCoverStorageKey();
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
