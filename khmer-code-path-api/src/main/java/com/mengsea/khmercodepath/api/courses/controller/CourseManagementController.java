package com.mengsea.khmercodepath.api.courses.controller;

import com.mengsea.khmercodepath.api.courses.payload.CoursePagePayload;
import com.mengsea.khmercodepath.api.courses.payload.CourseSummaryPayload;
import com.mengsea.khmercodepath.api.courses.payload.CreateCourseRequest;
import com.mengsea.khmercodepath.api.courses.payload.UpdateCourseRequest;
import com.mengsea.khmercodepath.api.courses.service.CourseManagementService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.CourseLevel;
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

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Catalog", description = "CRS — course catalog CRUD (AI-LMS)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class CourseManagementController {

    private final CourseManagementService courseManagementService;

    @Operation(summary = "CRS-2000 · List courses")
    @GetMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.CRS_READ + "')")
    public ResponseEntity<ApiResponse<CoursePagePayload>> listCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourseLevel level,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        CoursePagePayload data = courseManagementService.listCourses(search, level, pageable);
        return ResponseEntity.ok(ApiResponses.of("CRS-2000", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CRS-2010 · Get course")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CRS_READ + "')")
    public ResponseEntity<ApiResponse<CourseSummaryPayload>> getCourse(@PathVariable Long id) {
        CourseSummaryPayload data = courseManagementService.getCourse(id);
        return ResponseEntity.ok(ApiResponses.of("CRS-2010", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CRS-2020 · Create course")
    @PostMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.CRS_MANAGE + "')")
    public ResponseEntity<ApiResponse<CourseSummaryPayload>> createCourse(
            @Valid @RequestBody CreateCourseRequest request
    ) {
        CourseSummaryPayload data = courseManagementService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("CRS-2020", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "CRS-2030 · Update course")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CRS_MANAGE + "')")
    public ResponseEntity<ApiResponse<CourseSummaryPayload>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request
    ) {
        CourseSummaryPayload data = courseManagementService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponses.of("CRS-2030", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "CRS-2040 · Delete course")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CRS_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseManagementService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponses.of("CRS-2040", LmsStatusCode.SUCCESS, null, null));
    }
}
