package com.mengsea.khmercodepath.api.operations.payload;

import com.mengsea.khmercodepath.commons.constant.InfrastructureVariant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InfrastructureRowPayload {
    private String label;
    private String status;
    private InfrastructureVariant variant;
}
