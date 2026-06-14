package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonImprovePayload {
    private Long lessonId;
    private String improvedContent;
    private boolean persisted;
}
