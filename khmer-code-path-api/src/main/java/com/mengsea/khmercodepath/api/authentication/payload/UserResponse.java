package com.mengsea.khmercodepath.api.authentication.payload;

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
    private Role role;
}
