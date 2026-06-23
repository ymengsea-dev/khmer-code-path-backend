package com.mengsea.khmercodepath.api.auth.controller;

import com.mengsea.khmercodepath.api.auth.payload.AuthResponse;
import com.mengsea.khmercodepath.api.auth.payload.ChangePasswordRequest;
import com.mengsea.khmercodepath.api.auth.payload.LoginRequest;
import com.mengsea.khmercodepath.api.auth.payload.PasswordResetConfirmRequest;
import com.mengsea.khmercodepath.api.auth.payload.PasswordResetRequest;
import com.mengsea.khmercodepath.api.auth.payload.RefreshTokenRequest;
import com.mengsea.khmercodepath.api.auth.payload.RegisterRequest;
import com.mengsea.khmercodepath.api.auth.payload.UpdateProfileRequest;
import com.mengsea.khmercodepath.api.auth.payload.UserResponse;
import com.mengsea.khmercodepath.api.auth.service.UserService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@SecurityRequirements
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register User")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getSchoolSlug()
        );
        ApiResponse<Void> body = ApiResponses.of("AUTH-0090", LmsStatusCode.CREATED, null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        AuthResponse authResponse = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        addRefreshCookie(response, authResponse.getRefreshToken());
        ApiResponse<AuthResponse> body = ApiResponses.of("AUTH-0100", LmsStatusCode.SUCCESS, null, authResponse);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "AUTH-0110 · Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody(required = false) RefreshTokenRequest refreshRequest
    ) {
        String refreshToken = resolveRefreshToken(request, refreshRequest);
        AuthResponse authResponse = userService.refresh(refreshToken);
        if (authResponse.getRefreshToken() != null && !authResponse.getRefreshToken().isBlank()) {
            addRefreshCookie(response, authResponse.getRefreshToken());
        }
        ApiResponse<AuthResponse> apiBody =
                ApiResponses.of("AUTH-0110", LmsStatusCode.SUCCESS, null, authResponse);
        return ResponseEntity.ok(apiBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(getRefreshToken(request));
        clearRefreshCookie(response);
        ApiResponse<Void> body = ApiResponses.of("AUTH-0120", LmsStatusCode.SUCCESS, null, null);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        UserResponse me = userService.me(authentication.getName());
        ApiResponse<UserResponse> body = ApiResponses.of("AUTH-0130", LmsStatusCode.SUCCESS, null, me);
        return ResponseEntity.ok(body);
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse me = userService.updateProfile(authentication.getName(), request.getUserName(), request.getBio());
        ApiResponse<UserResponse> body = ApiResponses.of("AUTH-0131", LmsStatusCode.SUCCESS, null, me);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            ApiResponse<Void> body = ApiResponses.of(
                    "AUTH-0132",
                    LmsStatusCode.VALIDATION_FAILED,
                    "confirmPassword does not match newPassword",
                    null
            );
            return ResponseEntity.unprocessableEntity().body(body);
        }
        userService.changePassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());
        ApiResponse<Void> body = ApiResponses.of("AUTH-0132", LmsStatusCode.SUCCESS, null, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> passwordResetRequest(@Valid @RequestBody PasswordResetRequest request) {
        userService.requestPasswordReset(request.getEmail());
        ApiResponse<Void> body = ApiResponses.of("AUTH-0140", LmsStatusCode.SUCCESS, null, null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> passwordResetConfirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            ApiResponse<Void> body = ApiResponses.of("AUTH-0150", LmsStatusCode.VALIDATION_FAILED, "confirmPassword does not match newPassword", null);
            return ResponseEntity.unprocessableEntity().body(body);
        }
        userService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        ApiResponse<Void> body = ApiResponses.of("AUTH-0150", LmsStatusCode.SUCCESS, null, null);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Login with google")
    @GetMapping("/google")
    public void redirectToGoogle(
            @RequestParam(required = false) String schoolSlug,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        if (schoolSlug != null && !schoolSlug.isBlank()) {
            Cookie cookie = new Cookie("registration_school_slug", schoolSlug.trim().toLowerCase());
            cookie.setPath("/");
            cookie.setMaxAge(600);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            response.addCookie(cookie);
        }
        response.sendRedirect("/oauth2/authorization/google");
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("ailms_refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("ailms_refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String getRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> "ailms_refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String resolveRefreshToken(HttpServletRequest request, RefreshTokenRequest refreshRequest) {
        String fromCookie = getRefreshToken(request);
        if (fromCookie != null && !fromCookie.isBlank()) {
            return fromCookie;
        }
        if (refreshRequest != null
                && refreshRequest.getRefreshToken() != null
                && !refreshRequest.getRefreshToken().isBlank()) {
            return refreshRequest.getRefreshToken().trim();
        }
        return null;
    }
}
