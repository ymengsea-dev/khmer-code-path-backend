package com.mengsea.khmercodepath.api.ai.controller;

import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonAnswerPayload;
import com.mengsea.khmercodepath.api.ai.payload.LessonAskRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonImprovePayload;
import com.mengsea.khmercodepath.api.ai.payload.LessonImproveRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonSummaryGeneratePayload;
import com.mengsea.khmercodepath.api.ai.payload.MaterialRagStatusPayload;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;
import com.mengsea.khmercodepath.api.ai.service.LessonAiService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}")
@RequiredArgsConstructor
@Tag(name = "Lesson AI", description = "On-demand RAG: summary and quiz generation per material")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.AI_CHAT + "')")
public class LessonAiController {

    private final LessonAiService lessonAiService;

    @Operation(summary = "RAG index status for a lesson material")
    @GetMapping("/materials/{materialId}/rag/status")
    public ResponseEntity<ApiResponse<MaterialRagStatusPayload>> ragStatus(
            @PathVariable Long lessonId,
            @PathVariable Long materialId
    ) {
        MaterialRagStatusPayload data = lessonAiService.getMaterialRagStatus(lessonId, materialId);
        return ResponseEntity.ok(ApiResponses.of("LSN-0455", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Queue RAG indexing for a lesson material")
    @PostMapping("/materials/{materialId}/rag/index")
    @PreAuthorize("hasAuthority('" + LmsAuthority.AI_INGEST + "')")
    public ResponseEntity<ApiResponse<MaterialRagStatusPayload>> queueRagIndex(
            @PathVariable Long lessonId,
            @PathVariable Long materialId
    ) {
        MaterialRagStatusPayload data = lessonAiService.queueMaterialIndex(lessonId, materialId);
        return ResponseEntity.ok(ApiResponses.of("LSN-0456", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SUM-0600 · Generate lesson summary from a specific material (on-demand RAG)")
    @PostMapping("/summary")
    public ResponseEntity<ApiResponse<LessonSummaryGeneratePayload>> generateSummary(
            @PathVariable Long lessonId,
            @RequestParam Long materialId
    ) {
        LessonSummaryGeneratePayload data = lessonAiService.generateSummary(lessonId, materialId);
        return ResponseEntity.ok(ApiResponses.of("SUM-0600", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0500 · Generate quiz from a specific material (on-demand RAG)")
    @PostMapping("/quizzes/generate")
    @PreAuthorize("hasAuthority('" + LmsAuthority.AI_INGEST + "')")
    public ResponseEntity<ApiResponse<QuizGeneratePayload>> generateQuiz(
            @PathVariable Long lessonId,
            @Valid @RequestBody GenerateFromMaterialRequest request
    ) {
        QuizGeneratePayload data = lessonAiService.generateQuiz(lessonId, request);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0500", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SUM-0601 · Generate lesson summary from written notes (no file needed)")
    @PostMapping("/summary/from-content")
    public ResponseEntity<ApiResponse<LessonSummaryGeneratePayload>> generateSummaryFromContent(
            @PathVariable Long lessonId
    ) {
        LessonSummaryGeneratePayload data = lessonAiService.generateSummaryFromContent(lessonId);
        return ResponseEntity.ok(ApiResponses.of("SUM-0601", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Ask a lesson question with cited sources")
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<LessonAnswerPayload>> askWithCitations(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonAskRequest request
    ) {
        LessonAnswerPayload data = lessonAiService.answerWithCitations(
                lessonId,
                request.getMaterialId(),
                request.getQuestion()
        );
        return ResponseEntity.ok(ApiResponses.of("QNA-0710", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Improve lesson content with AI")
    @PostMapping("/improve")
    @PreAuthorize("hasAuthority('" + LmsAuthority.AI_INGEST + "')")
    public ResponseEntity<ApiResponse<LessonImprovePayload>> improveLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonImproveRequest request
    ) {
        LessonImprovePayload data = lessonAiService.improveLesson(lessonId, request);
        return ResponseEntity.ok(ApiResponses.of("LSN-0460", LmsStatusCode.SUCCESS, null, data));
    }
}
