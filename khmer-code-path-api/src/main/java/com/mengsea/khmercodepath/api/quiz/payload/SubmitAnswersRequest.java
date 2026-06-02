package com.mengsea.khmercodepath.api.quiz.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SubmitAnswersRequest {

    /** Map of questionId → selected option index (0-based). */
    @NotNull
    private Map<Long, Integer> answers;
}
