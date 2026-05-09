package com.mengsea.khmercodepath.commons.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiCommon {
    private String apiId;
    private String requestId;
}
