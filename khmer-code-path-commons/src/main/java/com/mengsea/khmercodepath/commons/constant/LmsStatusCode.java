package com.mengsea.khmercodepath.commons.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LmsStatusCode {
    SUCCESS("LMS-0000", "Success", HttpStatus.OK),
    CREATED("LMS-0001", "Created", HttpStatus.CREATED),
    BAD_REQUEST("LMS-1000", "Bad Request", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("LMS-1001", "Validation Failed", HttpStatus.UNPROCESSABLE_ENTITY),
    UNAUTHORIZED("LMS-2000", "Unauthorized", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("LMS-2001", "Token Expired", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("LMS-2002", "Refresh Token Expired", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("LMS-2003", "Forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("LMS-3000", "Not Found", HttpStatus.NOT_FOUND),
    CONFLICT("LMS-4000", "Conflict", HttpStatus.CONFLICT),
    OPERATION_NOT_ALLOWED("LMS-4002", "Operation Not Allowed", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR("LMS-9999", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
