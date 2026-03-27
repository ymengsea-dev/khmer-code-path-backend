package com.mengsea.khmercodepathbackend.services;

import com.mengsea.khmercodepathbackend.dto.advices.AuthResponse;

public interface UserService {
    void register(String username, String email, String password);
    AuthResponse login(String email, String password);
}
