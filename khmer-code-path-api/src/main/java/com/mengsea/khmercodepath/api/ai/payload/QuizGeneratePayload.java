package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuizGeneratePayload {
    Long lessonId;
    Long materialId;
    String sourceFileName;
    int questionCount;
    /** AI-generated quiz content (JSON or markdown) — persisted quiz entity is a follow-up. */
    String generatedContent;
}
