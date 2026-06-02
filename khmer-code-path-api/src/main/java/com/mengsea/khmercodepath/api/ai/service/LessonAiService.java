package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonSummaryGeneratePayload;
import com.mengsea.khmercodepath.api.ai.payload.MaterialRagStatusPayload;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;

public interface LessonAiService {

    MaterialRagStatusPayload getMaterialRagStatus(Long lessonId, Long materialId);

    LessonSummaryGeneratePayload generateSummary(Long lessonId, Long materialId);

    QuizGeneratePayload generateQuiz(Long lessonId, GenerateFromMaterialRequest request);

    /** Generate a summary directly from the lesson's description field — no uploaded file needed. */
    LessonSummaryGeneratePayload generateSummaryFromContent(Long lessonId);
}
