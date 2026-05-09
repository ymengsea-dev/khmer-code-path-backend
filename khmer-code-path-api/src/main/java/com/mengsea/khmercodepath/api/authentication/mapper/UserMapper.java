package com.mengsea.khmercodepath.api.authentication.mapper;

import com.mengsea.khmercodepath.api.authentication.payload.UserResponse;
import com.mengsea.khmercodepath.commons.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUuid())
                .userName(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
