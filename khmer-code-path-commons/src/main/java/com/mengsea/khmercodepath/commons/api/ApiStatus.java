package com.mengsea.khmercodepath.commons.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiStatus {
    private String code;
    private String message;
    private String detail;
}
