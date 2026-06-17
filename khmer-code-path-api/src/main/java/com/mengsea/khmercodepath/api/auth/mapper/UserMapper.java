package com.mengsea.khmercodepath.api.auth.mapper;

import com.mengsea.khmercodepath.api.auth.payload.UserResponse;
import com.mengsea.khmercodepath.commons.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUuid())
                .userName(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .bio(user.getBio())
                .avatarUrl(resolveAvatarUrl(user))
                .build();
    }

    private static String resolveAvatarUrl(User user) {
        if (user.getAvatarStorageKey() == null || user.getAvatarStorageKey().isBlank()) {
            return null;
        }
        return "/api/v1/profile/avatar/" + user.getUuid();
    }
}
