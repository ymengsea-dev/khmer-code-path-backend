package com.mengsea.khmercodepath.api.operations.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InfrastructurePayload {
    private List<InfrastructureRowPayload> systemHealth;
    private List<InfrastructureRowPayload> facilityStatus;
}
