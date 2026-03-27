package com.mengsea.khmercodepathbackend.dto.advices;

import com.mengsea.khmercodepathbackend.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthResponse {
    private  String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
}
