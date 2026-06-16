package com.mengsea.khmercodepath.api.quiz.controller;

import com.mengsea.khmercodepath.api.quiz.payload.PublishQuizRequest;
import com.mengsea.khmercodepath.api.quiz.payload.QuizAttemptResultDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizResultsDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizSummaryDto;
import com.mengsea.khmercodepath.api.quiz.payload.SubmitAnswersRequest;
import com.mengsea.khmercodepath.api.quiz.payload.UpdateQuizRequest;
import com.mengsea.khmercodepath.api.quiz.service.QuizService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "AI-generated quiz lifecycle: publish, take, submit")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "QUIZ-0600 · Teacher publishes an AI-generated quiz to a class")
    @PostMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<QuizDto>> publish(
            @Valid @RequestBody PublishQuizRequest request
    ) {
        QuizDto data = quizService.publish(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("QUIZ-0600", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "QUIZ-0601 · Teacher lists quizzes for their classes (optionally filtered by classId)")
    @GetMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<QuizDto>>> listForTeacher(
            @RequestParam(required = false) Long classId
    ) {
        List<QuizDto> data = quizService.listForTeacher(classId);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0601", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0602 · Student lists published quizzes assigned to their enrolled classes")
    @GetMapping("/assigned")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<QuizDto>>> listAssigned() {
        List<QuizDto> data = quizService.listAssigned();
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0602", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0603 · Get a single quiz with questions (correct answers hidden for students)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<QuizDto>> getQuiz(@PathVariable Long id) {
        QuizDto data = quizService.getQuiz(id);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0603", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0607 · Teacher reviews quiz submissions and results")
    @GetMapping("/{id}/results")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<QuizResultsDto>> getResults(@PathVariable Long id) {
        QuizResultsDto data = quizService.getResults(id);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0607", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0608 · Teacher updates a quiz before student attempts")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<QuizDto>> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuizRequest request
    ) {
        QuizDto data = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0608", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0604 · Student submits answers")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<QuizAttemptResultDto>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAnswersRequest request
    ) {
        QuizAttemptResultDto data = quizService.submit(id, request);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0604", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0606 · Teacher deletes (soft) a quiz they own")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0606", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "QUIZ-0609 · Aggregated quiz stats for the current user (role-aware)")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.LSN_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<QuizSummaryDto>> getSummary() {
        QuizSummaryDto data = quizService.getSummary();
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0609", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0605 · Student marks quiz as failed (tab-switch / cheating detection)")
    @PostMapping("/{id}/fail")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<Void>> fail(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        String reason = body != null ? body.getOrDefault("reason", "Tab switched") : "Tab switched";
        quizService.fail(id, reason);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0605", LmsStatusCode.SUCCESS, null, null));
    }
}
