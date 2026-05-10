package com.mengsea.khmercodepath.commons.security;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.CustomUserDetail;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException(ExceptionCode.UNAUTHORIZED);
        }
        if (auth.getPrincipal() instanceof CustomUserDetail detail) {
            return detail.getUser();
        }
        throw new BusinessException(ExceptionCode.UNAUTHORIZED);
    }
}
