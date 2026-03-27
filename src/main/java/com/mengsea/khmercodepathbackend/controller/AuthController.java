package com.mengsea.khmercodepathbackend.controller;

import com.mengsea.khmercodepathbackend.constant.ResponseCode;
import com.mengsea.khmercodepathbackend.dto.advices.ApiResponse;
import com.mengsea.khmercodepathbackend.dto.advices.ApiStatus;
import com.mengsea.khmercodepathbackend.dto.advices.AuthResponse;
import com.mengsea.khmercodepathbackend.dto.request.LoginRequest;
import com.mengsea.khmercodepathbackend.dto.request.RegisterRequest;
import com.mengsea.khmercodepathbackend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register User")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .status(ApiStatus.builder()
                        .code(ResponseCode.USER_CREATED.name())
                        .message(ResponseCode.USER_CREATED.getMessage())
                        .build())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        ApiResponse<AuthResponse> body = ApiResponse.<AuthResponse>builder()
                .status(ApiStatus.builder()
                        .code(ResponseCode.LOGIN_SUCCESS.name())
                        .message(ResponseCode.LOGIN_SUCCESS.getMessage())
                        .build())
                .data(authResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Login with google")
    @GetMapping("/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

}
