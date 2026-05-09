package com.mengsea.khmercodepathbackend.dto.advices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private ApiStatus status;
    private T data;
    private ApiCommon common;
}
