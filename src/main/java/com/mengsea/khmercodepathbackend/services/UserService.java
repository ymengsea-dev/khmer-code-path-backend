package com.mengsea.khmercodepathbackend.services;

import com.mengsea.khmercodepathbackend.dto.advices.AuthResponse;
import com.mengsea.khmercodepathbackend.dto.response.UserResponse;

public interface UserService {
    void register(String username, String email, String password);
    AuthResponse login(String email, String password);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
    UserResponse me(String email);
    void requestPasswordReset(String email);
    void confirmPasswordReset(String token, String newPassword);
}
