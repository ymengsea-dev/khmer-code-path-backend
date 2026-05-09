package com.mengsea.khmercodepath.api.authentication.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required.")
    @Email
    @Schema(description = "your email", example = "youremail@gmail.com")
    private String email;

    @NotBlank(message = "Password is required.")
    @Schema(description = "your password", example = "Pass123")
    private String password;
}
