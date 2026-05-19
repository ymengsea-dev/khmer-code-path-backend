package com.mengsea.khmercodepath.api.operations.payload;

import com.mengsea.khmercodepath.commons.constant.AssetStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhysicalAssetPayload {
    private Long id;
    private String name;
    private String category;
    private AssetStatus status;
    private String location;
    private String assignedTo;
}
