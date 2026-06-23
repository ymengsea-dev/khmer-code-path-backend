package com.mengsea.khmercodepath.api.schools.service;

import com.mengsea.khmercodepath.api.schools.payload.SchoolRegistrationInfoPayload;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.service.SchoolRegistrationSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SchoolRegistrationService {

    private final SchoolRegistrationSupport schoolRegistrationSupport;
    private final SchoolManagementServiceImpl schoolManagementService;

    @Transactional(readOnly = true)
    public SchoolRegistrationInfoPayload getRegistrationInfo(String slug) {
        School school = schoolRegistrationSupport.requireRegistrationSchool(slug);
        var domains = schoolRegistrationSupport.listAllowedDomains(school.getId());
        return SchoolRegistrationInfoPayload.builder()
                .schoolId(school.getId())
                .name(school.getName())
                .slug(school.getSlug())
                .registrationOpen(school.isRegistrationOpen())
                .tagline(school.getTagline())
                .coverUrl(schoolManagementService.resolveCoverUrl(school))
                .domainRequired(!domains.isEmpty())
                .allowedDomains(domains)
                .build();
    }

    @Transactional
    public void registerStudent(String schoolSlug, String username, String email, String password) {
        schoolRegistrationSupport.registerLocalStudent(schoolSlug, username, email, password);
    }

    @Transactional
    public User registerOAuthStudent(School school, String username, String email) {
        return schoolRegistrationSupport.registerOAuthStudent(school, username, email);
    }

    @Transactional(readOnly = true)
    public Optional<School> resolveSchoolForOAuthRegistration(String schoolSlug, String email) {
        return schoolRegistrationSupport.resolveSchoolForOAuthRegistration(schoolSlug, email);
    }
}
