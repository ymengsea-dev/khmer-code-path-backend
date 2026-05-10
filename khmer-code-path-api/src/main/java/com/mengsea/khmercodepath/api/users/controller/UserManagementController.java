package com.mengsea.khmercodepath.api.users.controller;

import com.mengsea.khmercodepath.api.users.payload.CreateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.UpdateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.api.users.payload.UserImportResultPayload;
import com.mengsea.khmercodepath.api.users.payload.UserPagePayload;
import com.mengsea.khmercodepath.api.users.payload.UserStatusRequest;
import com.mengsea.khmercodepath.api.users.service.UserManagementService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "USR — admin user management (AI-LMS API spec)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.USR_MANAGE + "')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(summary = "USR-0200 · List users")
    @GetMapping
    public ResponseEntity<ApiResponse<UserPagePayload>> listUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        UserPagePayload data = userManagementService.listUsers(name, email, role, isActive, includeDeleted, pageable);
        return ResponseEntity.ok(ApiResponses.of("USR-0200", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "USR-0210 · Get user by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailPayload>> getUser(@PathVariable String id) {
        UserDetailPayload data = userManagementService.getUser(id);
        return ResponseEntity.ok(ApiResponses.of("USR-0210", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "USR-0220 · Create user")
    @PostMapping
    public ResponseEntity<ApiResponse<UserDetailPayload>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDetailPayload data = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("USR-0220", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "USR-0230 · Update user")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailPayload>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserDetailPayload data = userManagementService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponses.of("USR-0230", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "USR-0240 · Delete user (soft)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.ok(ApiResponses.of("USR-0240", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "USR-0250 · Toggle user status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserDetailPayload>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UserStatusRequest request
    ) {
        UserDetailPayload data = userManagementService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponses.of("USR-0250", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "USR-0260 · Bulk import users (CSV or XLSX)")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserImportResultPayload>> importUsers(@RequestPart("file") MultipartFile file) {
        UserImportResultPayload data = userManagementService.importUsers(file);
        return ResponseEntity.ok(ApiResponses.of("USR-0260", LmsStatusCode.SUCCESS, null, data));
    }
}
