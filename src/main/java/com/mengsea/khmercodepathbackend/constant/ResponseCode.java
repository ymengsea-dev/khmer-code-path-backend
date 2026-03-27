package com.mengsea.khmercodepathbackend.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    // User
    USER_CREATED("CREATED",       201, "User created successfully."),

    // file
    FILE_UPLOAD("CREATED",       201, "File uploaded successfully."),

    // Auth
    LOGIN_SUCCESS("SUCCESS",      200, "Login successful."),
    LOGOUT_SUCCESS("SUCCESS",     200, "Logout successful."),
    TOKEN_REFRESHED("SUCCESS",    200, "Token refreshed successfully.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
