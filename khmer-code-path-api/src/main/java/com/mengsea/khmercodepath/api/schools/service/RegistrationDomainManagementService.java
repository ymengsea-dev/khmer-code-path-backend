package com.mengsea.khmercodepath.api.schools.service;

import com.mengsea.khmercodepath.api.schools.payload.CreateRegistrationDomainRequest;
import com.mengsea.khmercodepath.api.schools.payload.RegistrationDomainConfigPayload;
import com.mengsea.khmercodepath.api.schools.payload.RegistrationDomainPayload;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.RegistrationDomain;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.RegistrationDomainRepository;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import com.mengsea.khmercodepath.commons.util.EmailDomainUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationDomainManagementService {

    private final RegistrationDomainRepository registrationDomainRepository;
    private final SchoolAccessHelper schoolAccessHelper;

    @Transactional(readOnly = true)
    public RegistrationDomainConfigPayload getConfig() {
        schoolAccessHelper.assertSchoolAdmin(SecurityUtils.requireCurrentUser());
        return RegistrationDomainConfigPayload.builder()
                .pageTitle("Allowed email domains")
                .pageDescription(
                        "Optional. Restrict student signup to specific email domains. "
                                + "Your registration portal link and branding are configured under School Profile.")
                .domainInputLabel("Email domain")
                .domainInputPlaceholder("example.edu.kh")
                .addButtonLabel("Add domain")
                .emptyMessage("No email domains configured. Students can register with any email address.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<RegistrationDomainPayload> listDomains() {
        School school = schoolAccessHelper.requireSchool(SecurityUtils.requireCurrentUser());
        schoolAccessHelper.assertSchoolAdmin(SecurityUtils.requireCurrentUser());
        return registrationDomainRepository.findBySchool_IdAndDeletedFalseOrderByDomainAsc(school.getId())
                .stream()
                .map(this::toPayload)
                .toList();
    }

    @Transactional
    public RegistrationDomainPayload createDomain(CreateRegistrationDomainRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);

        String domain = EmailDomainUtils.normalizeDomain(request.getDomain());
        if (domain.isBlank() || !domain.contains(".")) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        if (registrationDomainRepository.existsByDomainIgnoreCaseAndDeletedFalse(domain)) {
            throw new BusinessException(ExceptionCode.REGISTRATION_DOMAIN_CONFLICT);
        }

        RegistrationDomain entity = new RegistrationDomain();
        entity.setSchool(school);
        entity.setDomain(domain);
        entity.setAutoApprove(true);
        entity.setDefaultRole(Role.STUDENT);
        entity.setDeleted(false);
        registrationDomainRepository.save(entity);
        return toPayload(entity);
    }

    @Transactional
    public void deleteDomain(Long id) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);
        RegistrationDomain entity = registrationDomainRepository
                .findByIdAndSchool_IdAndDeletedFalse(id, school.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.REGISTRATION_DOMAIN_NOT_FOUND));
        entity.setDeleted(true);
        registrationDomainRepository.save(entity);
    }

    private RegistrationDomainPayload toPayload(RegistrationDomain entity) {
        return RegistrationDomainPayload.builder()
                .id(entity.getId())
                .domain(entity.getDomain())
                .autoApprove(entity.isAutoApprove())
                .defaultRole(entity.getDefaultRole())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
