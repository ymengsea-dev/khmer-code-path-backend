package com.mengsea.khmercodepath.api.users.controller;

import com.mengsea.khmercodepath.api.users.payload.StudentDetailPayload;
import com.mengsea.khmercodepath.api.users.payload.StudentPagePayload;
import com.mengsea.khmercodepath.api.users.payload.UserManagementConfigPayload;
import com.mengsea.khmercodepath.api.users.service.UserManagementService;
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

@RestController
@RequestMapping("/api/v1/student-management")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "Student management UI config")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class StudentManagementController {

    private final UserManagementService userManagementService;

    @Operation(summary = "STM-0100 · Student management UI config (tabs, filters, actions)")
    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.USR_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<UserManagementConfigPayload>> getConfig() {
        UserManagementConfigPayload data = userManagementService.getConfig();
        return ResponseEntity.ok(ApiResponses.of("STM-0100", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "STM-0110 · List students (optional class filter)")
    @GetMapping("/students")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.USR_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<StudentPagePayload>> listStudents(
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive
    ) {
        StudentPagePayload data = userManagementService.listStudents(classId, search, isActive);
        return ResponseEntity.ok(ApiResponses.of("STM-0110", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "STM-0120 · Get student profile")
    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.USR_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<StudentDetailPayload>> getStudent(@PathVariable String id) {
        StudentDetailPayload data = userManagementService.getStudent(id);
        return ResponseEntity.ok(ApiResponses.of("STM-0120", LmsStatusCode.SUCCESS, null, data));
    }
}
