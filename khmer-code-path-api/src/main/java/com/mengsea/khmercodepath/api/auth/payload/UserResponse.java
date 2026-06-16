package com.mengsea.khmercodepath.api.auth.payload;

import com.mengsea.khmercodepath.commons.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String userId;
    private String userName;
    private String email;
    private Role role;
    private boolean isActive;
    private String bio;
}
