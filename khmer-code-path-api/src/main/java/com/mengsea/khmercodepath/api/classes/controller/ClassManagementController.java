package com.mengsea.khmercodepath.api.classes.controller;

import com.mengsea.khmercodepath.api.classes.payload.AssignStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.ClassCommentPayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassDetailPayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassInvitationPayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassPagePayload;
import com.mengsea.khmercodepath.api.classes.payload.CreateClassCommentRequest;
import com.mengsea.khmercodepath.api.classes.payload.CreateClassRequest;
import com.mengsea.khmercodepath.api.classes.payload.RemoveStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.UpdateClassRequest;
import com.mengsea.khmercodepath.api.classes.service.ClassCommentService;
import com.mengsea.khmercodepath.api.classes.service.ClassInvitationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Class Management", description = "CLS — class CRUD and enrollments (AI-LMS API spec)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class ClassManagementController {

    private final ClassManagementService classManagementService;
    private final ClassCommentService classCommentService;
    private final ClassInvitationService classInvitationService;

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

    @Operation(summary = "CLS-0350 · Invite students to class (notification + accept flow)")
    @PostMapping("/{id}/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<Void>> assignStudents(
            @PathVariable Long id,
            @Valid @RequestBody AssignStudentsRequest request
    ) {
        classManagementService.assignStudents(id, request);
        return ResponseEntity.ok(ApiResponses.of("CLS-0350", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "CLS-0360 · Remove students from class")
    @DeleteMapping("/{id}/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<Void>> removeStudents(
            @PathVariable Long id,
            @Valid @RequestBody RemoveStudentsRequest request
    ) {
        classManagementService.removeStudents(id, request);
        return ResponseEntity.ok(ApiResponses.of("CLS-0360", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "CLS-0355 · List my pending class invitations")
    @GetMapping("/invitations/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ClassInvitationPayload>>> myInvitations() {
        List<ClassInvitationPayload> data = classInvitationService.listMyPendingInvitations();
        return ResponseEntity.ok(ApiResponses.of("CLS-0355", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0356 · Accept class invitation")
    @PatchMapping("/invitations/{invitationId}/accept")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ClassInvitationPayload>> acceptInvitation(
            @PathVariable Long invitationId
    ) {
        ClassInvitationPayload data = classInvitationService.acceptInvitation(invitationId);
        return ResponseEntity.ok(ApiResponses.of("CLS-0356", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0357 · Decline class invitation")
    @PatchMapping("/invitations/{invitationId}/decline")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ClassInvitationPayload>> declineInvitation(
            @PathVariable Long invitationId
    ) {
        ClassInvitationPayload data = classInvitationService.declineInvitation(invitationId);
        return ResponseEntity.ok(ApiResponses.of("CLS-0357", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0358 · List pending invitations for a class")
    @GetMapping("/{id}/invitations")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<ClassInvitationPayload>>> listClassInvitations(
            @PathVariable Long id
    ) {
        List<ClassInvitationPayload> data = classInvitationService.listPendingInvitationsForClass(id);
        return ResponseEntity.ok(ApiResponses.of("CLS-0358", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0370 · List students in class")
    @GetMapping("/{id}/students")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<UserDetailPayload>>> listClassStudents(@PathVariable Long id) {
        List<UserDetailPayload> data = classManagementService.listClassStudents(id);
        return ResponseEntity.ok(ApiResponses.of("CLS-0370", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0380 · List class comments")
    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<ClassCommentPayload>>> listComments(@PathVariable Long id) {
        List<ClassCommentPayload> data = classCommentService.listComments(id);
        return ResponseEntity.ok(ApiResponses.of("CLS-0380", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CLS-0385 · Post class comment")
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<ClassCommentPayload>> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateClassCommentRequest request
    ) {
        ClassCommentPayload data = classCommentService.createComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("CLS-0385", LmsStatusCode.CREATED, null, data));
    }
}
