package com.mengsea.khmercodepathbackend.dto.response;

import com.mengsea.khmercodepathbackend.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String userName;
    private Role role;
}
