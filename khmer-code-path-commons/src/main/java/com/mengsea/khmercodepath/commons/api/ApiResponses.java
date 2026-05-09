package com.mengsea.khmercodepath.commons.api;

import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;

import java.util.UUID;

public final class ApiResponses {
    private ApiResponses() {}

    public static <T> ApiResponse<T> of(String apiId, LmsStatusCode code, String detail, T data) {
        return ApiResponse.<T>builder()
                .status(ApiStatus.builder()
                        .code(code.getCode())
                        .message(code.getMessage())
                        .detail(detail)
                        .build())
                .data(data)
                .common(ApiCommon.builder()
                        .apiId(apiId)
                        .requestId(UUID.randomUUID().toString())
                        .build())
                .build();
    }
}
