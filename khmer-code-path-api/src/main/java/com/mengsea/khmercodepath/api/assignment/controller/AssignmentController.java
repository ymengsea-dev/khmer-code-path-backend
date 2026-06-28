package com.mengsea.khmercodepath.api.assignment.controller;

import com.mengsea.khmercodepath.api.assignment.payload.AssignmentDto;
import com.mengsea.khmercodepath.api.assignment.payload.AssignmentSubmissionDto;
import com.mengsea.khmercodepath.api.assignment.payload.CreateAssignmentRequest;
import com.mengsea.khmercodepath.api.assignment.payload.SubmitAssignmentRequest;
import com.mengsea.khmercodepath.api.assignment.service.AssignmentService;
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

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Written assignments lifecycle")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "ASGN-0701 · Teacher creates an assignment")
    @PostMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<AssignmentDto>> create(
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        AssignmentDto data = assignmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("ASGN-0701", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "ASGN-0702 · Teacher lists assignments (optionally filtered by classId)")
    @GetMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<AssignmentDto>>> listForTeacher(
            @RequestParam(required = false) Long classId
    ) {
        List<AssignmentDto> data = assignmentService.listForTeacher(classId);
        return ResponseEntity.ok(ApiResponses.of("ASGN-0702", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ASGN-0703 · Student lists published assignments for enrolled classes")
    @GetMapping("/assigned")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<AssignmentDto>>> listAssigned() {
        List<AssignmentDto> data = assignmentService.listAssigned();
        return ResponseEntity.ok(ApiResponses.of("ASGN-0703", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ASGN-0704 · Get assignment detail")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.LSN_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<AssignmentDto>> getAssignment(@PathVariable Long id) {
        AssignmentDto data = assignmentService.getAssignment(id);
        return ResponseEntity.ok(ApiResponses.of("ASGN-0704", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ASGN-0705 · Student submits assignment content")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<AssignmentDto>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAssignmentRequest request
    ) {
        AssignmentDto data = assignmentService.submit(id, request);
        return ResponseEntity.ok(ApiResponses.of("ASGN-0705", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ASGN-0706 · Teacher reviews assignment submissions")
    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionDto>>> getSubmissions(@PathVariable Long id) {
        List<AssignmentSubmissionDto> data = assignmentService.getSubmissions(id);
        return ResponseEntity.ok(ApiResponses.of("ASGN-0706", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ASGN-0707 · Teacher soft-deletes an assignment")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponses.of("ASGN-0707", LmsStatusCode.SUCCESS, null, null));
    }
}
