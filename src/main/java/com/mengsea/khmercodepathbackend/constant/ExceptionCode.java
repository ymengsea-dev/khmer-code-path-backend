package com.mengsea.khmercodepathbackend.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    // user exceptions
    OAUTH2_LOGIN_FAILED(401,"Unable to login with google"),
    USER_NOT_FOUND(404, "User not found"), // 404: not found
    USER_ALREADY_EXISTS(409, "User already exists"), // 409: conflict
    USER_UNAUTHORIZED(401, "Unauthorized access"), // 401: unauthorized
    INVALID_CREDENTIAL(401, "Invalid username or password"),
    ACCOUNT_INACTIVE(401, "Account is inactive"),
    ACCOUNT_LOOKED(401, "Account is locked"),
    REFRESH_TOKEN_EXPIRED(400, "Refresh token was expired. Please make a new sign in request"),
    REFRESH_TOKEN_NOT_FOUND(400, "Refresh token not found."),
    REFRESH_TOKEN_REVOKED(400, "Refresh token has been revoked"),

    // product exceptions
    PRODUCT_NOT_FOUND(404, "Product not found"),
    PRODUCT_HAS_ACTIVE_ORDERS(400, "Product has active order"), // 400: bad request
    PRODUCT_ALREADY_EXISTS(409, "Product already exists"),

    // security
    UNAUTHORIZED(401, "Token is missing or invalid."),
    ACCESS_DENIED(403, "You do not have permission to perform this action."), // 403: forbidden
    VALIDATION_ERROR(400, "Request validation error"),
    INTERNAL_SERVER_ERROR(500, "Inter server error"),

    // file
    FILE_not_SUPPORT(400, "image must be a valid image URL ending with .png, .svg, .jpg, .jpeg, or .gif");
    private final int code;
    private  final String message;
}
