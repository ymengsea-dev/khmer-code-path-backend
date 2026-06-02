package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.gateway.LlmGateway;
import com.mengsea.khmercodepath.api.ai.payload.GenerateFromContentRequest;
import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonSummaryGeneratePayload;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;
import com.mengsea.khmercodepath.api.ai.rag.MaterialRagVectorService;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.MaterialSourceType;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryItem;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryMaterial;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryItemRepository;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryMaterialRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LibraryAiServiceImpl implements LibraryAiService {

    private final MaterialLibraryItemRepository materialLibraryItemRepository;
    private final MaterialLibraryMaterialRepository materialLibraryMaterialRepository;
    private final MaterialRagVectorService materialRagVectorService;
    private final LlmGateway llmGateway;

    @Override
    @Transactional(readOnly = true)
    public QuizGeneratePayload generateQuiz(Long libraryItemId, GenerateFromMaterialRequest request) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();
        MaterialLibraryItem item = materialLibraryItemRepository
                .findByIdAndTeacher_UuidAndDeletedFalse(libraryItemId, teacherUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND));

        MaterialLibraryMaterial material = materialLibraryMaterialRepository
                .findByIdAndDeletedFalse(request.getMaterialId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (!material.getLibraryItem().getId().equals(libraryItemId)) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }

        materialRagVectorService.ensureIndexed(MaterialSourceType.LIBRARY_MATERIAL, material.getId());

        String prompt = """
                Generate %d multiple-choice quiz questions from this lesson material only.
                Difficulty: %s.
                Return valid JSON array with objects: question, options (array of 4 strings), correctIndex (0-3), explanation.
                Do not invent facts outside the material.
                """.formatted(request.getQuestionCount(), request.getDifficulty());

        String generated = materialRagVectorService.queryMaterial(
                MaterialSourceType.LIBRARY_MATERIAL,
                material.getId(),
                prompt,
                12
        );

        return QuizGeneratePayload.builder()
                .lessonId(null)
                .materialId(material.getId())
                .sourceFileName(material.getFileName())
                .questionCount(request.getQuestionCount())
                .generatedContent(generated)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizGeneratePayload generateQuizFromContent(Long libraryItemId, GenerateFromContentRequest request) {
        MaterialLibraryItem item = requireOwnedItem(libraryItemId);
        String plainText = stripHtml(item.getDescription());

        if (plainText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "This template has no written notes. Add content in the Course Content editor first.");
        }

        String systemPrompt = """
                Generate %d multiple-choice quiz questions based solely on the lesson content below.
                Difficulty: %s.
                Return a valid JSON array where each element has:
                  "question" (string),
                  "options" (array of exactly 4 strings),
                  "correctIndex" (integer 0-3),
                  "explanation" (string).
                Do not invent facts outside the provided content.
                """.formatted(request.getQuestionCount(), request.getDifficulty());

        String generated = llmGateway.completeWithContent(systemPrompt, plainText);

        return QuizGeneratePayload.builder()
                .lessonId(null)
                .materialId(null)
                .sourceFileName(item.getTitle() + " (written notes)")
                .questionCount(request.getQuestionCount())
                .generatedContent(generated)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonSummaryGeneratePayload generateSummaryFromContent(Long libraryItemId) {
        MaterialLibraryItem item = requireOwnedItem(libraryItemId);
        String plainText = stripHtml(item.getDescription());

        if (plainText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "This template has no written notes. Add content in the Course Content editor first.");
        }

        String systemPrompt = """
                You are an expert instructor. Read the lesson content below and write a clear, structured summary.
                Use short headings and bullet points where helpful. Focus on key concepts only.
                Do not invent facts outside the provided content.
                """;

        String summary = llmGateway.completeWithContent(systemPrompt, plainText);

        return LessonSummaryGeneratePayload.builder()
                .lessonId(null)
                .materialId(null)
                .summary(summary)
                .sourceFileName(item.getTitle() + " (written notes)")
                .persisted(false)
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private MaterialLibraryItem requireOwnedItem(Long libraryItemId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();
        return materialLibraryItemRepository
                .findByIdAndTeacher_UuidAndDeletedFalse(libraryItemId, teacherUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND));
    }

    /** Strip HTML tags and collapse whitespace to produce plain text for the LLM. */
    private static String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("&[a-zA-Z]+;", " ").replaceAll("\\s+", " ").trim();
    }
}
