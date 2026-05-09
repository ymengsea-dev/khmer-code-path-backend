package com.mengsea.khmercodepathbackend.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    OAUTH2_LOGIN_FAILED(LmsStatusCode.UNAUTHORIZED, "Unable to login with google"),
    USER_NOT_FOUND(LmsStatusCode.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS(LmsStatusCode.CONFLICT, "User already exists"),
    USER_UNAUTHORIZED(LmsStatusCode.UNAUTHORIZED, "Unauthorized access"),
    INVALID_CREDENTIAL(LmsStatusCode.UNAUTHORIZED, "Invalid username or password"),
    ACCOUNT_INACTIVE(LmsStatusCode.UNAUTHORIZED, "Account is inactive"),
    ACCOUNT_LOOKED(LmsStatusCode.UNAUTHORIZED, "Account is locked"),
    REFRESH_TOKEN_EXPIRED(LmsStatusCode.REFRESH_TOKEN_EXPIRED, "Refresh token expired, login again"),
    REFRESH_TOKEN_NOT_FOUND(LmsStatusCode.REFRESH_TOKEN_EXPIRED, "Refresh token not found."),
    REFRESH_TOKEN_REVOKED(LmsStatusCode.REFRESH_TOKEN_EXPIRED, "Refresh token has been revoked"),
    UNAUTHORIZED(LmsStatusCode.UNAUTHORIZED, "Token is missing or invalid."),
    ACCESS_DENIED(LmsStatusCode.FORBIDDEN, "You do not have permission to perform this action."),
    VALIDATION_ERROR(LmsStatusCode.VALIDATION_FAILED, "Request validation error"),
    INTERNAL_SERVER_ERROR(LmsStatusCode.INTERNAL_SERVER_ERROR, "Internal server error"),
    TOKEN_EXPIRED(LmsStatusCode.TOKEN_EXPIRED, "Access token has expired"),
    PASSWORD_RESET_TOKEN_INVALID(LmsStatusCode.BAD_REQUEST, "Password reset token is invalid"),
    PASSWORD_RESET_TOKEN_EXPIRED(LmsStatusCode.BAD_REQUEST, "Password reset token has expired");

    private final LmsStatusCode statusCode;
    private final String message;

    public HttpStatus getHttpStatus() {
        return statusCode.getHttpStatus();
    }

    public String getCode() {
        return statusCode.getCode();
    }
}
