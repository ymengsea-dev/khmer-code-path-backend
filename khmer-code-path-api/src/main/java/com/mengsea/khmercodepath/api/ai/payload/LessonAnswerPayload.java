package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LessonAnswerPayload {
    private Long lessonId;
    private String answer;
    private List<LessonCitationPayload> citations;
}
