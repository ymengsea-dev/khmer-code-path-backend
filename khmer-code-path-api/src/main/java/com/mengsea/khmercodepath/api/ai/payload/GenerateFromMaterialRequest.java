package com.mengsea.khmercodepath.api.ai.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateFromMaterialRequest {

    @NotNull
    private Long materialId;

    @Min(1)
    @Max(30)
    private int questionCount = 10;

    private String difficulty = "medium";
}
