package com.mengsea.khmercodepath.api.permissions.controller;

import com.mengsea.khmercodepath.api.permissions.payload.PermissionsConfigPayload;
import com.mengsea.khmercodepath.api.permissions.payload.RolePermissionsPayload;
import com.mengsea.khmercodepath.api.permissions.payload.SchoolFeaturesPayload;
import com.mengsea.khmercodepath.api.permissions.payload.UpdateRolePermissionsRequest;
import com.mengsea.khmercodepath.api.permissions.payload.UpdateSchoolFeaturesRequest;
import com.mengsea.khmercodepath.api.permissions.service.TeacherPermissionManagementService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schools/me/permissions")
@RequiredArgsConstructor
@Tag(name = "School Permissions", description = "SCH — school-wide role permission grants (school admin)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class SchoolPermissionsController {

    private final TeacherPermissionManagementService teacherPermissionManagementService;

    @Operation(summary = "SCH-1300 · Permissions UI config")
    @GetMapping("/config")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<PermissionsConfigPayload>> getConfig() {
        PermissionsConfigPayload data = teacherPermissionManagementService.getConfig();
        return ResponseEntity.ok(ApiResponses.of("SCH-1300", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1310 · Get school-wide teacher permissions")
    @GetMapping("/teachers")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<RolePermissionsPayload>> getTeacherPermissions() {
        RolePermissionsPayload data = teacherPermissionManagementService.getTeacherPermissions();
        return ResponseEntity.ok(ApiResponses.of("SCH-1310", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1320 · Update school-wide teacher permissions")
    @PutMapping("/teachers")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<RolePermissionsPayload>> updateTeacherPermissions(
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        RolePermissionsPayload data = teacherPermissionManagementService.updateTeacherPermissions(request);
        return ResponseEntity.ok(ApiResponses.of("SCH-1320", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1330 · Get school-wide student permissions")
    @GetMapping("/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<RolePermissionsPayload>> getStudentPermissions() {
        RolePermissionsPayload data = teacherPermissionManagementService.getStudentPermissions();
        return ResponseEntity.ok(ApiResponses.of("SCH-1330", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1340 · Update school-wide student permissions")
    @PutMapping("/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<RolePermissionsPayload>> updateStudentPermissions(
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        RolePermissionsPayload data = teacherPermissionManagementService.updateStudentPermissions(request);
        return ResponseEntity.ok(ApiResponses.of("SCH-1340", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1350 · Get school feature toggles")
    @GetMapping("/school-features")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<SchoolFeaturesPayload>> getSchoolFeatures() {
        SchoolFeaturesPayload data = teacherPermissionManagementService.getSchoolFeatures();
        return ResponseEntity.ok(ApiResponses.of("SCH-1350", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1360 · Update school feature toggles")
    @PutMapping("/school-features")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<SchoolFeaturesPayload>> updateSchoolFeatures(
            @Valid @RequestBody UpdateSchoolFeaturesRequest request
    ) {
        SchoolFeaturesPayload data = teacherPermissionManagementService.updateSchoolFeatures(request);
        return ResponseEntity.ok(ApiResponses.of("SCH-1360", LmsStatusCode.SUCCESS, null, data));
    }
}
