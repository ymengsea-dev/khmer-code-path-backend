package com.mengsea.khmercodepath.commons.security;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolProvisioningService {

    public static final String DEFAULT_SCHOOL_SLUG = "default";

    private final SchoolRepository schoolRepository;

    public School requireDefaultSchool() {
        return schoolRepository.findBySlugAndDeletedFalse(DEFAULT_SCHOOL_SLUG)
                .orElseThrow(() -> new BusinessException(ExceptionCode.SCHOOL_NOT_FOUND));
    }

    public void assignDefaultSchoolIfMissing(User user) {
        if (user.getSchool() == null) {
            user.setSchool(requireDefaultSchool());
        }
    }
}
