package com.mengsea.khmercodepath.api.dashboard.controller;

import com.mengsea.khmercodepath.api.dashboard.payload.AdminDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.StudentDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.TeacherDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.service.DashboardService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "DASH — role-based dashboard metrics")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "DASH-1320 · Student dashboard")
    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardPayload>> studentDashboard() {
        StudentDashboardPayload data = dashboardService.getStudentDashboard();
        return ResponseEntity.ok(ApiResponses.of("DASH-1320", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "DASH-1310 · Teacher dashboard")
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherDashboardPayload>> teacherDashboard() {
        TeacherDashboardPayload data = dashboardService.getTeacherDashboard();
        return ResponseEntity.ok(ApiResponses.of("DASH-1310", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "DASH-1300 · Admin dashboard")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardPayload>> adminDashboard() {
        AdminDashboardPayload data = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponses.of("DASH-1300", LmsStatusCode.SUCCESS, null, data));
    }
}
