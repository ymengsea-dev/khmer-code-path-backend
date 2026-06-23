package com.mengsea.khmercodepath.api.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Schema(description = "username", example = "your name")
    private String username;

    @Email
    @NotBlank(message = "Email is required")
    @Schema(description = "your email", example = "youremail@gmail.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 16)
    @Schema(description = "your password", example = "Pass123", minLength = 4, maxLength = 16)
    private String password;

    @NotBlank(message = "School slug is required")
    @Schema(description = "School registration slug", example = "default")
    private String schoolSlug;
}
