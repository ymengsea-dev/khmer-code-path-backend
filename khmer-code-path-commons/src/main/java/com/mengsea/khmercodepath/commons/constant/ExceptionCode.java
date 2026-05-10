package com.mengsea.khmercodepath.commons.constant;

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
    PASSWORD_RESET_TOKEN_EXPIRED(LmsStatusCode.BAD_REQUEST, "Password reset token has expired"),
    DUPLICATE_STUDENT_ID(LmsStatusCode.CONFLICT, "Student ID is already in use"),
    DUPLICATE_TEACHER_ID(LmsStatusCode.CONFLICT, "Teacher ID is already in use"),
    IMMUTABLE_USER_FIELD(LmsStatusCode.OPERATION_NOT_ALLOWED, "This field cannot be changed after creation"),
    USER_ALREADY_DELETED(LmsStatusCode.NOT_FOUND, "User not found or already removed"),
    UNSUPPORTED_IMPORT_FORMAT(LmsStatusCode.BAD_REQUEST, "Only CSV or XLSX files are supported"),
    IMPORT_FILE_EMPTY(LmsStatusCode.BAD_REQUEST, "Import file is empty or invalid"),
    CLASS_NOT_FOUND(LmsStatusCode.NOT_FOUND, "Class not found"),
    CLASS_CODE_CONFLICT(LmsStatusCode.CONFLICT, "Class code is already in use"),
    CLASS_DELETE_NOT_ALLOWED(LmsStatusCode.OPERATION_NOT_ALLOWED,
            "Class can be deleted only when it has no enrolled students and no lessons"),
    TEACHER_NOT_FOUND(LmsStatusCode.BAD_REQUEST, "Teacher user not found or is not a teacher"),
    STUDENT_NOT_FOUND(LmsStatusCode.BAD_REQUEST, "One or more student users were not found or are not students");

    private final LmsStatusCode statusCode;
    private final String message;

    public HttpStatus getHttpStatus() {
        return statusCode.getHttpStatus();
    }

    public String getCode() {
        return statusCode.getCode();
    }
}
