package com.mengsea.khmercodepath.api.schools.controller;

import com.mengsea.khmercodepath.api.schools.payload.SchoolDetailPayload;
import com.mengsea.khmercodepath.api.schools.payload.UpdateSchoolRequest;
import com.mengsea.khmercodepath.api.schools.service.SchoolCoverService;
import com.mengsea.khmercodepath.api.schools.service.SchoolManagementService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
@Tag(name = "School Management", description = "SCH — school tenant profile and settings")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class SchoolManagementController {

    private final SchoolManagementService schoolManagementService;
    private final SchoolCoverService schoolCoverService;
    private final SchoolAccessHelper schoolAccessHelper;

    @Operation(summary = "SCH-1000 · Get current user's school")
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.DASH_READ + "')")
    public ResponseEntity<ApiResponse<SchoolDetailPayload>> getMySchool() {
        SchoolDetailPayload data = schoolManagementService.getMySchool();
        return ResponseEntity.ok(ApiResponses.of("SCH-1000", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1010 · Update current school (school admin)")
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<SchoolDetailPayload>> updateMySchool(
            @Valid @RequestBody UpdateSchoolRequest request
    ) {
        SchoolDetailPayload data = schoolManagementService.updateMySchool(request);
        return ResponseEntity.ok(ApiResponses.of("SCH-1010", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1030 · Upload school cover image")
    @PostMapping("/me/cover")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<SchoolDetailPayload>> uploadCover(
            @RequestParam("file") MultipartFile file
    ) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);
        schoolCoverService.uploadCover(school, file);
        SchoolDetailPayload data = schoolManagementService.getMySchool();
        return ResponseEntity.ok(ApiResponses.of("SCH-1030", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1031 · Remove school cover image")
    @DeleteMapping("/me/cover")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<SchoolDetailPayload>> removeCover() {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);
        schoolCoverService.removeCover(school);
        SchoolDetailPayload data = schoolManagementService.getMySchool();
        return ResponseEntity.ok(ApiResponses.of("SCH-1031", LmsStatusCode.SUCCESS, null, data));
    }
}
