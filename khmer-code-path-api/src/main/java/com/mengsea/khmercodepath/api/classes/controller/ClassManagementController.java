package com.mengsea.khmercodepath.api.classes.controller;

import com.mengsea.khmercodepath.api.classes.payload.AssignStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.ClassDetailPayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassPagePayload;
import com.mengsea.khmercodepath.api.classes.payload.CreateClassRequest;
import com.mengsea.khmercodepath.api.classes.payload.RemoveStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.UpdateClassRequest;
import com.mengsea.khmercodepath.api.classes.service.ClassManagementService;
import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Class Management", description = "CLS — class CRUD and enrollments (AI-LMS API spec)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class ClassManagementController {

    private final ClassManagementService classManagementService;

    @Operation(summary = "CLS-0300 · List classes")
    @GetMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<ClassPagePayload>> listClasses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) ClassStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ClassPagePayload data = classManagementService.listClasses(search, teacherId, semester, academicYear, status, pageable);
        return ResponseEntity.ok(ApiResponses.of("CLS-0300", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0310 · Get class details")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<ClassDetailPayload>> getClass(@PathVariable Long id) {
        ClassDetailPayload data = classManagementService.getClass(id);
        return ResponseEntity.ok(ApiResponses.of("CLS-0310", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0320 · Create class")
    @PostMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_MANAGE + "')")
    public ResponseEntity<ApiResponse<ClassDetailPayload>> createClass(@Valid @RequestBody CreateClassRequest request) {
        ClassDetailPayload data = classManagementService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("CLS-0320", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "CLS-0330 · Update class")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_MANAGE + "')")
    public ResponseEntity<ApiResponse<ClassDetailPayload>> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassRequest request
    ) {
        ClassDetailPayload data = classManagementService.updateClass(id, request);
        return ResponseEntity.ok(ApiResponses.of("CLS-0330", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0340 · Delete class")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Long id) {
        classManagementService.deleteClass(id);
        return ResponseEntity.ok(ApiResponses.of("CLS-0340", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "CLS-0350 · Assign students to class")
    @PostMapping("/{id}/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> assignStudents(
            @PathVariable Long id,
            @Valid @RequestBody AssignStudentsRequest request
    ) {
        classManagementService.assignStudents(id, request);
        return ResponseEntity.ok(ApiResponses.of("CLS-0350", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "CLS-0360 · Remove students from class")
    @DeleteMapping("/{id}/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> removeStudents(
            @PathVariable Long id,
            @Valid @RequestBody RemoveStudentsRequest request
    ) {
        classManagementService.removeStudents(id, request);
        return ResponseEntity.ok(ApiResponses.of("CLS-0360", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "CLS-0370 · List students in class")
    @GetMapping("/{id}/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<UserDetailPayload>>> listClassStudents(@PathVariable Long id) {
        List<UserDetailPayload> data = classManagementService.listClassStudents(id);
        return ResponseEntity.ok(ApiResponses.of("CLS-0370", LmsStatusCode.SUCCESS, null, data));
    }
}
