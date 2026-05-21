package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;

public interface LibraryAiService {

    QuizGeneratePayload generateQuiz(Long libraryItemId, GenerateFromMaterialRequest request);
}
