package com.mengsea.khmercodepath.api.schools.service;

import com.mengsea.khmercodepath.api.schools.config.SchoolProperties;
import com.mengsea.khmercodepath.api.schools.payload.SchoolConfigPayload;
import com.mengsea.khmercodepath.api.schools.payload.SchoolConfigTabPayload;
import com.mengsea.khmercodepath.api.schools.payload.SchoolDetailPayload;
import com.mengsea.khmercodepath.api.schools.payload.SchoolProfileConfigPayload;
import com.mengsea.khmercodepath.api.schools.payload.UpdateSchoolRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.SchoolRepository;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SchoolManagementServiceImpl implements SchoolManagementService {

    private static final List<SchoolConfigTabPayload> ADMIN_TABS = List.of();

    private final SchoolRepository schoolRepository;
    private final SchoolAccessHelper schoolAccessHelper;
    private final SchoolProperties schoolProperties;

    @Override
    @Transactional(readOnly = true)
    public SchoolDetailPayload getMySchool() {
        User me = SecurityUtils.requireCurrentUser();
        School school = schoolAccessHelper.requireSchool(me);
        return toDetail(school);
    }

    @Override
    @Transactional
    public SchoolDetailPayload updateMySchool(UpdateSchoolRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);

        if (request.getName() != null && !request.getName().isBlank()) {
            school.setName(request.getName().trim());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String slug = normalizeSlug(request.getSlug());
            if (!slug.equalsIgnoreCase(school.getSlug())
                    && schoolRepository.existsBySlugIgnoreCaseAndDeletedFalseAndIdNot(slug, school.getId())) {
                throw new BusinessException(ExceptionCode.SCHOOL_SLUG_CONFLICT);
            }
            school.setSlug(slug);
        }
        if (request.getStatus() != null) {
            school.setStatus(request.getStatus());
        }
        if (request.getRegistrationOpen() != null) {
            school.setRegistrationOpen(request.getRegistrationOpen());
        }
        if (request.getTagline() != null) {
            school.setTagline(request.getTagline().isBlank() ? null : request.getTagline().trim());
        }

        schoolRepository.save(school);
        return toDetail(school);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolConfigPayload getSchoolConfig() {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        return SchoolConfigPayload.builder()
                .pageTitle("School Management")
                .pageDescription("")
                .tabs(ADMIN_TABS)
                .profile(SchoolProfileConfigPayload.builder()
                        .profileSectionTitle(schoolProperties.getProfileSectionTitle())
                        .profileSectionDescription(schoolProperties.getProfileSectionDescription())
                        .nameLabel(schoolProperties.getNameLabel())
                        .slugLabel(schoolProperties.getSlugLabel())
                        .taglineLabel(schoolProperties.getTaglineLabel())
                        .taglinePlaceholder(schoolProperties.getTaglinePlaceholder())
                        .registrationOpenLabel(schoolProperties.getRegistrationOpenLabel())
                        .saveProfileLabel(schoolProperties.getSaveProfileLabel())
                        .coverImageLabel(schoolProperties.getCoverImageLabel())
                        .coverImageDescription(schoolProperties.getCoverImageDescription())
                        .uploadCoverLabel(schoolProperties.getUploadCoverLabel())
                        .removeCoverLabel(schoolProperties.getRemoveCoverLabel())
                        .registrationUrlLabel(schoolProperties.getRegistrationUrlLabel())
                        .copyUrlLabel(schoolProperties.getCopyUrlLabel())
                        .copiedUrlMessage(schoolProperties.getCopiedUrlMessage())
                        .registrationPathPrefix(schoolProperties.getRegistrationPathPrefix())
                        .build())
                .build();
    }

    public SchoolDetailPayload toDetail(School school) {
        return SchoolDetailPayload.builder()
                .id(school.getId())
                .name(school.getName())
                .slug(school.getSlug())
                .status(school.getStatus().name())
                .registrationOpen(school.isRegistrationOpen())
                .tagline(school.getTagline())
                .coverUrl(resolveCoverUrl(school))
                .registrationPath(resolveRegistrationPath(school))
                .registrationUrl(resolveRegistrationUrl(school))
                .createdAt(school.getCreatedAt())
                .updatedAt(school.getUpdatedAt())
                .build();
    }

    public String resolveRegistrationPath(School school) {
        return schoolProperties.getRegistrationPathPrefix() + school.getSlug();
    }

    public String resolveRegistrationUrl(School school) {
        String base = schoolProperties.getPublicBaseUrl().replaceAll("/+$", "");
        return base + resolveRegistrationPath(school);
    }

    public String resolveCoverUrl(School school) {
        if (school.getCoverStorageKey() == null || school.getCoverStorageKey().isBlank()) {
            return null;
        }
        return "/api/v1/schools/register/" + school.getSlug() + "/cover";
    }

    private static String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]+", "-");
    }
}
