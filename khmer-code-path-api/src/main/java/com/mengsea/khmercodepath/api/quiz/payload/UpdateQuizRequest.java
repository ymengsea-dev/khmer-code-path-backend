package com.mengsea.khmercodepath.api.quiz.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQuizRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String generatedContent;

    @Min(1)
    private int questionCount = 10;

    private Integer durationMinutes;
}
