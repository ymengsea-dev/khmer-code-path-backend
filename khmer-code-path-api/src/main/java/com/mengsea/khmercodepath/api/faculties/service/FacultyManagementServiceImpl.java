package com.mengsea.khmercodepath.api.faculties.service;

import com.mengsea.khmercodepath.api.faculties.config.FacultiesProperties;
import com.mengsea.khmercodepath.api.faculties.payload.CreateFacultyRequest;
import com.mengsea.khmercodepath.api.faculties.payload.FacultyConfigPayload;
import com.mengsea.khmercodepath.api.faculties.payload.FacultySummaryPayload;
import com.mengsea.khmercodepath.api.faculties.payload.UpdateFacultyRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.FacultyStatus;
import com.mengsea.khmercodepath.commons.domain.Faculty;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.DepartmentRepository;
import com.mengsea.khmercodepath.commons.repository.FacultyRepository;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyManagementServiceImpl implements FacultyManagementService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final SchoolAccessHelper schoolAccessHelper;
    private final FacultiesProperties facultiesProperties;

    @Override
    @Transactional(readOnly = true)
    public FacultyConfigPayload getConfig() {
        schoolAccessHelper.assertSchoolAdmin(SecurityUtils.requireCurrentUser());
        return FacultyConfigPayload.builder()
                .pageTitle(facultiesProperties.getPageTitle())
                .pageDescription(facultiesProperties.getPageDescription())
                .sectionTitle(facultiesProperties.getSectionTitle())
                .sectionDescription(facultiesProperties.getSectionDescription())
                .nameLabel(facultiesProperties.getNameLabel())
                .taglineLabel(facultiesProperties.getTaglineLabel())
                .taglinePlaceholder(facultiesProperties.getTaglinePlaceholder())
                .addButtonLabel(facultiesProperties.getAddButtonLabel())
                .saveButtonLabel(facultiesProperties.getSaveButtonLabel())
                .configureButtonLabel(facultiesProperties.getConfigureButtonLabel())
                .emptyMessage(facultiesProperties.getEmptyMessage())
                .departmentCountLabel(facultiesProperties.getDepartmentCountLabel())
                .coverImageLabel(facultiesProperties.getCoverImageLabel())
                .coverImageDescription(facultiesProperties.getCoverImageDescription())
                .uploadCoverLabel(facultiesProperties.getUploadCoverLabel())
                .removeCoverLabel(facultiesProperties.getRemoveCoverLabel())
                .configureDialogTitle(facultiesProperties.getConfigureDialogTitle())
                .configureDialogDescription(facultiesProperties.getConfigureDialogDescription())
                .backToFacultiesLabel(facultiesProperties.getBackToFacultiesLabel())
                .searchPlaceholder(facultiesProperties.getSearchPlaceholder())
                .noResultsMessage(facultiesProperties.getNoResultsMessage())
                .cardGradients(facultiesProperties.getCardGradients())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultySummaryPayload> listFaculties() {
        School school = schoolAccessHelper.requireSchool(SecurityUtils.requireCurrentUser());
        return facultyRepository.findBySchool_IdAndDeletedFalseOrderByNameAsc(school.getId()).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public FacultySummaryPayload createFaculty(CreateFacultyRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);

        String name = request.getName().trim();
        if (facultyRepository.existsBySchool_IdAndNameIgnoreCaseAndDeletedFalse(school.getId(), name)) {
            throw new BusinessException(ExceptionCode.FACULTY_NAME_CONFLICT);
        }

        Faculty entity = new Faculty();
        entity.setSchool(school);
        entity.setName(name);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : FacultyStatus.ACTIVE);
        entity.setDeleted(false);
        facultyRepository.save(entity);
        return toSummary(entity);
    }

    @Override
    @Transactional
    public FacultySummaryPayload updateFaculty(Long id, UpdateFacultyRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);

        Faculty entity = facultyRepository.findByIdAndSchool_IdAndDeletedFalse(id, school.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FACULTY_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            String name = request.getName().trim();
            if (facultyRepository.existsBySchool_IdAndNameIgnoreCaseAndDeletedFalseAndIdNot(
                    school.getId(), name, id)) {
                throw new BusinessException(ExceptionCode.FACULTY_NAME_CONFLICT);
            }
            entity.setName(name);
        }
        if (request.getTagline() != null) {
            String tagline = request.getTagline().trim();
            entity.setTagline(tagline.isEmpty() ? null : tagline);
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        facultyRepository.save(entity);
        return toSummary(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FacultySummaryPayload getFacultySummary(School school, Long facultyId) {
        Faculty faculty = facultyRepository.findByIdAndSchool_IdAndDeletedFalse(facultyId, school.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FACULTY_NOT_FOUND));
        return toSummary(faculty);
    }

    public String resolveCoverUrl(Faculty faculty) {
        if (faculty.getCoverStorageKey() == null || faculty.getCoverStorageKey().isBlank()) {
            return null;
        }
        return "/api/v1/schools/me/faculties/" + faculty.getId() + "/cover";
    }

    private FacultySummaryPayload toSummary(Faculty faculty) {
        int departmentCount = (int) departmentRepository.countByFacultyEntity_IdAndDeletedFalse(faculty.getId());
        return FacultySummaryPayload.builder()
                .id(faculty.getId())
                .name(faculty.getName())
                .tagline(faculty.getTagline())
                .coverUrl(resolveCoverUrl(faculty))
                .status(faculty.getStatus())
                .departmentCount(departmentCount)
                .build();
    }
}
