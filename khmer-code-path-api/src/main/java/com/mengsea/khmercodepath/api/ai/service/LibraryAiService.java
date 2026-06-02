package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.payload.GenerateFromContentRequest;
import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonSummaryGeneratePayload;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;

public interface LibraryAiService {

    QuizGeneratePayload generateQuiz(Long libraryItemId, GenerateFromMaterialRequest request);

    /** Generate a quiz directly from the template's written description — no uploaded file needed. */
    QuizGeneratePayload generateQuizFromContent(Long libraryItemId, GenerateFromContentRequest request);

    /** Generate a summary directly from the template's written description — no uploaded file needed. */
    LessonSummaryGeneratePayload generateSummaryFromContent(Long libraryItemId);
}
