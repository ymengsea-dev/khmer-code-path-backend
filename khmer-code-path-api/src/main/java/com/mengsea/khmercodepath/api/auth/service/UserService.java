package com.mengsea.khmercodepath.api.auth.service;

import com.mengsea.khmercodepath.api.auth.payload.AuthResponse;
import com.mengsea.khmercodepath.api.auth.payload.UserResponse;

public interface UserService {
    void register(String username, String email, String password);

    AuthResponse login(String email, String password);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    UserResponse me(String email);

    UserResponse updateProfile(String email, String userName);

    void changePassword(String email, String currentPassword, String newPassword);

    void requestPasswordReset(String email);

    void confirmPasswordReset(String token, String newPassword);
}
