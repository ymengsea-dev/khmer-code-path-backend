package com.mengsea.khmercodepath.api.authentication.service;

import com.mengsea.khmercodepath.api.authentication.payload.AuthResponse;
import com.mengsea.khmercodepath.api.authentication.payload.UserResponse;

public interface UserService {
    void register(String username, String email, String password);

    AuthResponse login(String email, String password);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    UserResponse me(String email);

    void requestPasswordReset(String email);

    void confirmPasswordReset(String token, String newPassword);
}
