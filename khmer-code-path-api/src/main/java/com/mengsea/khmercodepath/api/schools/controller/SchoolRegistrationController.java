package com.mengsea.khmercodepath.api.schools.controller;

import com.mengsea.khmercodepath.api.schools.payload.CreateRegistrationDomainRequest;
import com.mengsea.khmercodepath.api.schools.payload.RegistrationDomainConfigPayload;
import com.mengsea.khmercodepath.api.schools.payload.RegistrationDomainPayload;
import com.mengsea.khmercodepath.api.schools.payload.SchoolRegistrationInfoPayload;
import com.mengsea.khmercodepath.api.schools.service.RegistrationDomainManagementService;
import com.mengsea.khmercodepath.api.schools.service.SchoolCoverService;
import com.mengsea.khmercodepath.api.schools.service.SchoolRegistrationService;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
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
import org.springframework.http.CacheControl;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
@Tag(name = "School Registration", description = "SCH — public signup and registration domains")
public class SchoolRegistrationController {

    private final SchoolRegistrationService schoolRegistrationService;
    private final RegistrationDomainManagementService registrationDomainManagementService;
    private final SchoolCoverService schoolCoverService;

    @Operation(summary = "SCH-1100 · Public registration info for a school")
    @GetMapping("/register/{slug}")
    public ResponseEntity<ApiResponse<SchoolRegistrationInfoPayload>> getRegistrationInfo(
            @PathVariable String slug
    ) {
        SchoolRegistrationInfoPayload data = schoolRegistrationService.getRegistrationInfo(slug);
        return ResponseEntity.ok(ApiResponses.of("SCH-1100", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1105 · Public school cover image")
    @GetMapping("/register/{slug}/cover")
    public ResponseEntity<byte[]> getRegistrationCover(@PathVariable String slug) {
        SchoolCoverService.CoverResource cover = schoolCoverService.getCoverBySlug(slug);
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

    @Operation(summary = "SCH-1200 · List registration domains (school admin)")
    @GetMapping("/me/registration-domains")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<List<RegistrationDomainPayload>>> listRegistrationDomains() {
        List<RegistrationDomainPayload> data = registrationDomainManagementService.listDomains();
        return ResponseEntity.ok(ApiResponses.of("SCH-1200", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1201 · Registration domain UI config")
    @GetMapping("/me/registration-domains/config")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<RegistrationDomainConfigPayload>> getRegistrationDomainConfig() {
        RegistrationDomainConfigPayload data = registrationDomainManagementService.getConfig();
        return ResponseEntity.ok(ApiResponses.of("SCH-1201", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SCH-1210 · Add registration domain")
    @PostMapping("/me/registration-domains")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<RegistrationDomainPayload>> createRegistrationDomain(
            @Valid @RequestBody CreateRegistrationDomainRequest request
    ) {
        RegistrationDomainPayload data = registrationDomainManagementService.createDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("SCH-1210", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "SCH-1220 · Remove registration domain")
    @DeleteMapping("/me/registration-domains/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.SCHOOL_MANAGE + "')")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<Void>> deleteRegistrationDomain(@PathVariable Long id) {
        registrationDomainManagementService.deleteDomain(id);
        return ResponseEntity.ok(ApiResponses.of("SCH-1220", LmsStatusCode.SUCCESS, null, null));
    }
}
