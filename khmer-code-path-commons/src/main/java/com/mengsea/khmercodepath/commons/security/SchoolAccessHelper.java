package com.mengsea.khmercodepath.commons.security;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SchoolAccessHelper {

    public School requireSchool(User user) {
        if (user.getSchool() == null) {
            throw new BusinessException(ExceptionCode.SCHOOL_NOT_ASSIGNED);
        }
        return user.getSchool();
    }

    public Long requireSchoolId(User user) {
        return requireSchool(user).getId();
    }

    public boolean isSameSchool(User left, User right) {
        if (left.getSchool() == null || right.getSchool() == null) {
            return false;
        }
        return Objects.equals(left.getSchool().getId(), right.getSchool().getId());
    }

    public boolean isSameSchool(User user, LmsClass lmsClass) {
        if (user.getSchool() == null || lmsClass.getSchool() == null) {
            return false;
        }
        return Objects.equals(user.getSchool().getId(), lmsClass.getSchool().getId());
    }

    public void assertSameSchool(User actor, User target) {
        if (!isSameSchool(actor, target)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
    }

    public void assertSameSchool(User actor, LmsClass lmsClass) {
        if (!isSameSchool(actor, lmsClass)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
    }

    public void assertSchoolAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
        requireSchool(user);
    }
}
