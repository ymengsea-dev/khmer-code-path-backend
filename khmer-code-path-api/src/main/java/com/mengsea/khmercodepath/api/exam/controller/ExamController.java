package com.mengsea.khmercodepath.api.exam.controller;

import com.mengsea.khmercodepath.api.exam.payload.CreateExamRequest;
import com.mengsea.khmercodepath.api.exam.payload.ExamAttemptResultDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamResultsDto;
import com.mengsea.khmercodepath.api.exam.payload.SubmitExamAnswersRequest;
import com.mengsea.khmercodepath.api.exam.service.ExamService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Proctored MCQ exams lifecycle")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "EXAM-0801 · Teacher creates an exam")
    @PostMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<ExamDto>> create(
            @Valid @RequestBody CreateExamRequest request
    ) {
        ExamDto data = examService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("EXAM-0801", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "EXAM-0802 · Teacher lists exams (optionally filtered by classId)")
    @GetMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<ExamDto>>> listForTeacher(
            @RequestParam(required = false) Long classId
    ) {
        List<ExamDto> data = examService.listForTeacher(classId);
        return ResponseEntity.ok(ApiResponses.of("EXAM-0802", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "EXAM-0803 · Student lists published exams for enrolled classes")
    @GetMapping("/assigned")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<ExamDto>>> listAssigned() {
        List<ExamDto> data = examService.listAssigned();
        return ResponseEntity.ok(ApiResponses.of("EXAM-0803", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "EXAM-0804 · Get exam detail with questions")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<ExamDto>> getExam(@PathVariable Long id) {
        ExamDto data = examService.getExam(id);
        return ResponseEntity.ok(ApiResponses.of("EXAM-0804", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "EXAM-0805 · Student submits exam answers")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<ExamAttemptResultDto>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitExamAnswersRequest request
    ) {
        ExamAttemptResultDto data = examService.submit(id, request);
        return ResponseEntity.ok(ApiResponses.of("EXAM-0805", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "EXAM-0806 · Student marks exam as failed (proctoring violation)")
    @PostMapping("/{id}/fail")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<Void>> fail(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.getOrDefault("reason", "Tab switched") : "Tab switched";
        examService.fail(id, reason);
        return ResponseEntity.ok(ApiResponses.of("EXAM-0806", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "EXAM-0807 · Teacher reviews exam results")
    @GetMapping("/{id}/results")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<ExamResultsDto>> getResults(@PathVariable Long id) {
        ExamResultsDto data = examService.getResults(id);
        return ResponseEntity.ok(ApiResponses.of("EXAM-0807", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "EXAM-0808 · Teacher soft-deletes an exam")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok(ApiResponses.of("EXAM-0808", LmsStatusCode.SUCCESS, null, null));
    }
}
