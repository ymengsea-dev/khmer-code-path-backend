package com.mengsea.khmercodepath.api.ai.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonAskRequest {
    @NotBlank
    private String question;
    private Long materialId;
}
