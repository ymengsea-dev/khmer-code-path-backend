package com.mengsea.khmercodepath.api.faculties.controller;

import com.mengsea.khmercodepath.api.faculties.payload.CreateFacultyRequest;
import com.mengsea.khmercodepath.api.faculties.payload.FacultyConfigPayload;
import com.mengsea.khmercodepath.api.faculties.payload.FacultySummaryPayload;
import com.mengsea.khmercodepath.api.faculties.payload.UpdateFacultyRequest;
import com.mengsea.khmercodepath.api.faculties.service.FacultyCoverService;
import com.mengsea.khmercodepath.api.faculties.service.FacultyManagementService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schools/me/faculties")
@RequiredArgsConstructor
@Tag(name = "School Faculties", description = "FAC — school faculty divisions (school admin)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class SchoolFacultyController {

    private final FacultyManagementService facultyManagementService;
    private final FacultyCoverService facultyCoverService;
    private final SchoolAccessHelper schoolAccessHelper;

    @Operation(summary = "FAC-1400 · Faculties UI config")
    @GetMapping("/config")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<FacultyConfigPayload>> getConfig() {
        FacultyConfigPayload data = facultyManagementService.getConfig();
        return ResponseEntity.ok(ApiResponses.of("FAC-1400", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "FAC-1410 · List school faculties")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.SCHOOL_MANAGE + "','" + LmsAuthority.CLS_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<FacultySummaryPayload>>> listFaculties() {
        List<FacultySummaryPayload> data = facultyManagementService.listFaculties();
        return ResponseEntity.ok(ApiResponses.of("FAC-1410", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "FAC-1420 · Create faculty")
    @PostMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<FacultySummaryPayload>> createFaculty(
            @Valid @RequestBody CreateFacultyRequest request
    ) {
        FacultySummaryPayload data = facultyManagementService.createFaculty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("FAC-1420", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "FAC-1430 · Update faculty")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<FacultySummaryPayload>> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFacultyRequest request
    ) {
        FacultySummaryPayload data = facultyManagementService.updateFaculty(id, request);
        return ResponseEntity.ok(ApiResponses.of("FAC-1430", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "FAC-1440 · Upload faculty cover image")
    @PostMapping("/{id}/cover")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<FacultySummaryPayload>> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);
        facultyCoverService.uploadCover(school, id, file);
        FacultySummaryPayload data = facultyManagementService.getFacultySummary(school, id);
        return ResponseEntity.ok(ApiResponses.of("FAC-1440", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "FAC-1441 · Remove faculty cover image")
    @DeleteMapping("/{id}/cover")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    public ResponseEntity<ApiResponse<FacultySummaryPayload>> removeCover(@PathVariable Long id) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);
        facultyCoverService.removeCover(school, id);
        FacultySummaryPayload data = facultyManagementService.getFacultySummary(school, id);
        return ResponseEntity.ok(ApiResponses.of("FAC-1441", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "FAC-1445 · Faculty cover image")
    @GetMapping("/{id}/cover")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.SCHOOL_MANAGE + "','" + LmsAuthority.CLS_MANAGE + "')")
    public ResponseEntity<byte[]> getCover(@PathVariable Long id) {
        User me = SecurityUtils.requireCurrentUser();
        School school = schoolAccessHelper.requireSchool(me);
        FacultyCoverService.CoverResource cover = facultyCoverService.getCover(school, id);
        try {
            byte[] bytes = cover.resource().getInputStream().readAllBytes();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(cover.contentType())
                    .body(bytes);
        } catch (java.io.IOException ex) {
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
    }
}
