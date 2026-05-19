package com.mengsea.khmercodepath.api.grades.controller;

import com.mengsea.khmercodepath.api.grades.payload.CreateGradeRequest;
import com.mengsea.khmercodepath.api.grades.payload.FinalGradePayload;
import com.mengsea.khmercodepath.api.grades.payload.GradePayload;
import com.mengsea.khmercodepath.api.grades.payload.GradebookPayload;
import com.mengsea.khmercodepath.api.grades.payload.UpdateGradeRequest;
import com.mengsea.khmercodepath.api.grades.service.GradeManagementService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "GRD — student grades (V3 tables)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class GradeManagementController {

    private final GradeManagementService gradeManagementService;

    @Operation(summary = "GRD-1110 · Record grade")
    @PostMapping("/grades")
    @PreAuthorize("hasAuthority('" + LmsAuthority.GRD_MANAGE + "')")
    public ResponseEntity<ApiResponse<GradePayload>> createGrade(
            @Valid @RequestBody CreateGradeRequest request
    ) {
        GradePayload data = gradeManagementService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("GRD-1110", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "GRD-1120 · Update grade")
    @PutMapping("/grades/{gradeId}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.GRD_MANAGE + "')")
    public ResponseEntity<ApiResponse<GradePayload>> updateGrade(
            @PathVariable Long gradeId,
            @Valid @RequestBody UpdateGradeRequest request
    ) {
        GradePayload data = gradeManagementService.updateGrade(gradeId, request);
        return ResponseEntity.ok(ApiResponses.of("GRD-1120", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "GRD-1130 · Class gradebook")
    @GetMapping("/classes/{classId}/gradebook")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<GradebookPayload>> getGradebook(@PathVariable Long classId) {
        GradebookPayload data = gradeManagementService.getGradebook(classId);
        return ResponseEntity.ok(ApiResponses.of("GRD-1130", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "GRD-1140 · Student grades in class")
    @GetMapping("/classes/{classId}/students/{studentId}/grades")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<GradePayload>>> getStudentGrades(
            @PathVariable Long classId,
            @PathVariable String studentId
    ) {
        List<GradePayload> data = gradeManagementService.getStudentGrades(classId, studentId);
        return ResponseEntity.ok(ApiResponses.of("GRD-1140", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "GRD-1150 · Calculate final grade")
    @PostMapping("/classes/{classId}/students/{studentId}/final-grade/calculate")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<FinalGradePayload>> calculateFinalGrade(
            @PathVariable Long classId,
            @PathVariable String studentId
    ) {
        FinalGradePayload data = gradeManagementService.calculateFinalGrade(classId, studentId);
        return ResponseEntity.ok(ApiResponses.of("GRD-1150", LmsStatusCode.SUCCESS, null, data));
    }
}
