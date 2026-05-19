package com.mengsea.khmercodepath.api.operations.payload;

import com.mengsea.khmercodepath.commons.constant.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePhysicalAssetRequest {

    @NotBlank
    @Size(max = 500)
    private String name;

    @NotBlank
    @Size(max = 128)
    private String category;

    @NotNull
    private AssetStatus status;

    @NotBlank
    @Size(max = 255)
    private String location;

    @Size(max = 255)
    private String assignedTo;
}
