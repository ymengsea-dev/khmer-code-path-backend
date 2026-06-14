package com.mengsea.khmercodepath.api.profile.controller;

import com.mengsea.khmercodepath.api.profile.payload.MyLearningPayload;
import com.mengsea.khmercodepath.api.profile.payload.ProfileSummaryPayload;
import com.mengsea.khmercodepath.api.profile.service.ProfileSummaryService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Profile Summary", description = "Aggregated page data for learning and profile views")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class ProfileSummaryController {

    private final ProfileSummaryService profileSummaryService;

    @Operation(summary = "PAGE-1000 · Aggregated My Learning page data")
    @GetMapping("/learning/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<MyLearningPayload>> myLearning() {
        MyLearningPayload data = profileSummaryService.getMyLearning();
        return ResponseEntity.ok(ApiResponses.of("PAGE-1000", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "PAGE-1010 · Aggregated profile page data")
    @GetMapping("/profile/summary")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<ProfileSummaryPayload>> profileSummary() {
        ProfileSummaryPayload data = profileSummaryService.getProfileSummary();
        return ResponseEntity.ok(ApiResponses.of("PAGE-1010", LmsStatusCode.SUCCESS, null, data));
    }
}
