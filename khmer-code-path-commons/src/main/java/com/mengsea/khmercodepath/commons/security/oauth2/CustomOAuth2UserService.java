package com.mengsea.khmercodepath.commons.security.oauth2;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Provider;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.CustomOauthUser;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.service.SchoolRegistrationSupport;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final SchoolRegistrationSupport schoolRegistrationSupport;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found");
        }
        User user = userRepository.findByEmail(email)
                .map(u -> reactivateIfDeleted(u, name))
                .orElseGet(() -> createGoogleUser(email, name));

        clearRegistrationSchoolCookie();

        return new CustomOauthUser(user, oAuth2User.getAttributes());
    }

    private User reactivateIfDeleted(User user, String name) {
        if (user.isDeleted()) {
            user.setDeleted(false);
            user.setActive(true);
            if (name != null && !name.isBlank()) {
                user.setUsername(name);
            }
            return userRepository.save(user);
        }
        return user;
    }

    private User createGoogleUser(String email, String name) {
        String schoolSlug = readRegistrationSchoolSlugCookie();
        Optional<School> school = schoolRegistrationSupport.resolveSchoolForOAuthRegistration(schoolSlug, email);
        String displayName = name != null && !name.isBlank() ? name : email;

        if (school.isPresent()) {
            try {
                return schoolRegistrationSupport.registerOAuthStudent(school.get(), displayName, email);
            } catch (com.mengsea.khmercodepath.commons.exception.BusinessException ex) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        ex.getExceptionCode().getCode(),
                        ex.getMessage(),
                        null
                ));
            }
        }

        if (schoolSlug != null && !schoolSlug.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    ExceptionCode.SCHOOL_NOT_FOUND.getCode(),
                    ExceptionCode.SCHOOL_NOT_FOUND.getMessage(),
                    null
            ));
        }

        return schoolRegistrationSupport.registerOAuthStudentWithDefaultSchool(displayName, email);
    }

    private String readRegistrationSchoolSlugCookie() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("registration_school_slug".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearRegistrationSchoolCookie() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        Cookie cookie = new Cookie("registration_school_slug", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        attributes.getResponse().addCookie(cookie);
    }
}
