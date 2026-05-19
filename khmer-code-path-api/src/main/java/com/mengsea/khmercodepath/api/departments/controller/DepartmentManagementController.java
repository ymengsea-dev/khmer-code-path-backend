package com.mengsea.khmercodepath.api.departments.controller;

import com.mengsea.khmercodepath.api.departments.payload.CreateDepartmentRequest;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentDetailPayload;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentSummaryPayload;
import com.mengsea.khmercodepath.api.departments.payload.UpdateDepartmentRequest;
import com.mengsea.khmercodepath.api.departments.service.DepartmentManagementService;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "DEPT — academic departments (admin)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.OPS_MANAGE + "')")
public class DepartmentManagementController {

    private final DepartmentManagementService departmentManagementService;

    @Operation(summary = "DEPT-1700 · List departments")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentSummaryPayload>>> listDepartments() {
        List<DepartmentSummaryPayload> data = departmentManagementService.listDepartments();
        return ResponseEntity.ok(ApiResponses.of("DEPT-1700", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "DEPT-1720 · Get department details")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDetailPayload>> getDepartment(@PathVariable Long id) {
        DepartmentDetailPayload data = departmentManagementService.getDepartment(id);
        return ResponseEntity.ok(ApiResponses.of("DEPT-1720", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "DEPT-1710 · Create department")
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentSummaryPayload>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request
    ) {
        DepartmentSummaryPayload data = departmentManagementService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("DEPT-1710", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "DEPT-1730 · Update department")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentSummaryPayload>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        DepartmentSummaryPayload data = departmentManagementService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponses.of("DEPT-1730", LmsStatusCode.SUCCESS, null, data));
    }
}
