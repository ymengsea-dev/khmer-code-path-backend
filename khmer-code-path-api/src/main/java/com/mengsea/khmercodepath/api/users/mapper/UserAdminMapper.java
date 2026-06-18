package com.mengsea.khmercodepath.api.users.mapper;

import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.commons.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserAdminMapper {

    public UserDetailPayload toDetail(User user) {
        return UserDetailPayload.builder()
                .id(user.getUuid())
                .name(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .avatarUrl(resolveAvatarUrl(user))
                .studentId(user.getStudentId())
                .teacherId(user.getTeacherId())
                .provider(user.getProvider())
                .deleted(user.isDeleted())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private static String resolveAvatarUrl(User user) {
        if (user.getAvatarStorageKey() == null || user.getAvatarStorageKey().isBlank()) {
            return null;
        }
        return "/api/v1/profile/avatar/" + user.getUuid();
    }
}
