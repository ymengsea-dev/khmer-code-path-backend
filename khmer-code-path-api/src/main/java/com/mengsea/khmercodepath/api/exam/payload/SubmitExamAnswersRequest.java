package com.mengsea.khmercodepath.api.exam.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SubmitExamAnswersRequest {

    @NotNull
    private Map<Long, Integer> answers;
}
