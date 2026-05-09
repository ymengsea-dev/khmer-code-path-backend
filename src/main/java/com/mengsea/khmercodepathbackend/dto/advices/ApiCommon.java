package com.mengsea.khmercodepathbackend.dto.advices;

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

