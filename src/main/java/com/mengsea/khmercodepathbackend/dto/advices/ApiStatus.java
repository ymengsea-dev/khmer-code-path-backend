package com.mengsea.khmercodepathbackend.dto.advices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiStatus {
    private String code;
    private String message;
}
