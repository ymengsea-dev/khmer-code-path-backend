package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LessonSummaryGeneratePayload {
    Long lessonId;
    Long materialId;
    String summary;
    String sourceFileName;
    /** True when saved on the lesson record (teachers); false for student-only preview. */
    boolean persisted;
}
