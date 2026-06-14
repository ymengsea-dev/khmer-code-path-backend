package com.mengsea.khmercodepath.api.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetMailer {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendResetLink(String email, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        // Production deployments can replace this service with SMTP/provider delivery.
        log.info("Password reset link for {}: {}", email, resetUrl);
    }
}
