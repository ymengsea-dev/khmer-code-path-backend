package com.mengsea.khmercodepath.api.ai.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Request for AI generation from written text content (no uploaded file required).
 * Used for quiz generation and summary from template description / lesson notes.
 */
@Getter
@Setter
public class GenerateFromContentRequest {

    @Min(1)
    @Max(30)
    private int questionCount = 10;

    private String difficulty = "medium";
}
