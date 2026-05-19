package com.mengsea.khmercodepath.api.progress.controller;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.progress.payload.ClassProgressPayload;
import com.mengsea.khmercodepath.api.progress.payload.GradeBreakdownPayload;
import com.mengsea.khmercodepath.api.progress.payload.ProgressDashboardPayload;
import com.mengsea.khmercodepath.api.progress.payload.QuizHistoryPayload;
import com.mengsea.khmercodepath.api.progress.service.ProgressService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "Student Progress", description = "PROG — progress from V3 academic tables")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.PROG_READ + "')")
public class ProgressController {

    private final ProgressService progressService;

    @Operation(summary = "PROG-0800 · Student progress dashboard")
    @GetMapping("/students/{studentId}/dashboard")
    public ResponseEntity<ApiResponse<ProgressDashboardPayload>> dashboard(
            @PathVariable String studentId
    ) {
        ProgressDashboardPayload data = progressService.getDashboard(studentId);
        return ResponseEntity.ok(ApiResponses.of("PROG-0800", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "PROG-0810 · Class progress")
    @GetMapping("/students/{studentId}/classes/{classId}")
    public ResponseEntity<ApiResponse<ClassProgressPayload>> classProgress(
            @PathVariable String studentId,
            @PathVariable Long classId
    ) {
        ClassProgressPayload data = progressService.getClassProgress(studentId, classId);
        return ResponseEntity.ok(ApiResponses.of("PROG-0810", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "PROG-0820 · Quiz history")
    @GetMapping("/students/{studentId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizHistoryPayload>>> quizzes(
            @PathVariable String studentId
    ) {
        List<QuizHistoryPayload> data = progressService.getQuizHistory(studentId);
        return ResponseEntity.ok(ApiResponses.of("PROG-0820", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "PROG-0830 · Attendance history")
    @GetMapping("/students/{studentId}/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceRecordPayload>>> attendance(
            @PathVariable String studentId,
            @RequestParam(required = false) Long classId
    ) {
        List<AttendanceRecordPayload> data = progressService.getAttendance(studentId, classId);
        return ResponseEntity.ok(ApiResponses.of("PROG-0830", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "PROG-0840 · Grade breakdown")
    @GetMapping("/students/{studentId}/grades/breakdown")
    public ResponseEntity<ApiResponse<List<GradeBreakdownPayload>>> gradeBreakdown(
            @PathVariable String studentId
    ) {
        List<GradeBreakdownPayload> data = progressService.getGradeBreakdown(studentId);
        return ResponseEntity.ok(ApiResponses.of("PROG-0840", LmsStatusCode.SUCCESS, null, data));
    }
}
