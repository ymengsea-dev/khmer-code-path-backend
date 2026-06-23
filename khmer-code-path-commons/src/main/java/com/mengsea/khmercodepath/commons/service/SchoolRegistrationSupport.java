package com.mengsea.khmercodepath.commons.service;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Provider;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.constant.SchoolStatus;
import com.mengsea.khmercodepath.commons.domain.RegistrationDomain;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.RegistrationDomainRepository;
import com.mengsea.khmercodepath.commons.repository.SchoolRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SchoolProvisioningService;
import com.mengsea.khmercodepath.commons.util.EmailDomainUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SchoolRegistrationSupport {

    private final SchoolRepository schoolRepository;
    private final RegistrationDomainRepository registrationDomainRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolProvisioningService schoolProvisioningService;

    @Transactional(readOnly = true)
    public School requireRegistrationSchool(String slug) {
        return schoolRepository.findBySlugAndDeletedFalse(normalizeSlug(slug))
                .orElseThrow(() -> new BusinessException(ExceptionCode.SCHOOL_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<String> listAllowedDomains(Long schoolId) {
        return registrationDomainRepository.findBySchool_IdAndDeletedFalseOrderByDomainAsc(schoolId)
                .stream()
                .map(RegistrationDomain::getDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasConfiguredDomains(Long schoolId) {
        return registrationDomainRepository.existsBySchool_IdAndDeletedFalse(schoolId);
    }

    @Transactional
    public void registerLocalStudent(String schoolSlug, String username, String email, String password) {
        School school = requireRegistrationSchool(schoolSlug);
        assertRegistrationOpen(school);
        assertEmailAllowedForSchool(school, email);

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new BusinessException(ExceptionCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider(Provider.LOCAL);
        user.setRole(Role.STUDENT);
        user.setActive(true);
        user.setDeleted(false);
        user.setSchool(school);
        userRepository.save(user);
    }

    @Transactional
    public User registerOAuthStudent(School school, String username, String email) {
        assertRegistrationOpen(school);
        assertEmailAllowedForSchool(school, email);

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = new User();
        user.setUsername(username);
        user.setEmail(normalizedEmail);
        user.setPassword(null);
        user.setProvider(Provider.GOOGLE);
        user.setRole(Role.STUDENT);
        user.setActive(true);
        user.setDeleted(false);
        user.setSchool(school);
        return userRepository.save(user);
    }

    @Transactional
    public User registerOAuthStudentWithDefaultSchool(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email.trim().toLowerCase(Locale.ROOT));
        user.setPassword(null);
        user.setProvider(Provider.GOOGLE);
        user.setRole(Role.STUDENT);
        user.setDeleted(false);
        schoolProvisioningService.assignDefaultSchoolIfMissing(user);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<School> resolveSchoolForOAuthRegistration(String schoolSlug, String email) {
        if (schoolSlug != null && !schoolSlug.isBlank()) {
            return schoolRepository.findBySlugAndDeletedFalse(normalizeSlug(schoolSlug));
        }
        String domain = EmailDomainUtils.extractDomain(email);
        if (domain.isBlank()) {
            return Optional.empty();
        }
        return registrationDomainRepository.findByDomainIgnoreCaseAndDeletedFalse(domain)
                .map(RegistrationDomain::getSchool);
    }

    public void assertEmailAllowedForSchool(School school, String email) {
        List<RegistrationDomain> domains = registrationDomainRepository
                .findBySchool_IdAndDeletedFalseOrderByDomainAsc(school.getId());
        if (domains.isEmpty()) {
            return;
        }
        String domain = EmailDomainUtils.extractDomain(email);
        boolean allowed = domains.stream()
                .anyMatch(item -> item.getDomain().equalsIgnoreCase(domain));
        if (!allowed) {
            throw new BusinessException(ExceptionCode.REGISTRATION_DOMAIN_NOT_ALLOWED);
        }
    }

    private void assertRegistrationOpen(School school) {
        if (school.getStatus() != SchoolStatus.ACTIVE || !school.isRegistrationOpen()) {
            throw new BusinessException(ExceptionCode.REGISTRATION_CLOSED);
        }
    }

    private static String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}
