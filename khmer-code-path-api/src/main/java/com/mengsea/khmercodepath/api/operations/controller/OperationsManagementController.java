package com.mengsea.khmercodepath.api.operations.controller;

import com.mengsea.khmercodepath.api.operations.payload.CreatePhysicalAssetRequest;
import com.mengsea.khmercodepath.api.operations.payload.FacultyRequestPayload;
import com.mengsea.khmercodepath.api.operations.payload.InfrastructurePayload;
import com.mengsea.khmercodepath.api.operations.payload.PhysicalAssetPayload;
import com.mengsea.khmercodepath.api.operations.payload.UpdateFacultyRequestStatusRequest;
import com.mengsea.khmercodepath.api.operations.service.OperationsManagementService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import com.mengsea.khmercodepath.commons.constant.RequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
@Tag(name = "School Operations", description = "OPS — inventory, requests, infrastructure (admin)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.OPS_MANAGE + "')")
public class OperationsManagementController {

    private final OperationsManagementService operationsManagementService;

    @Operation(summary = "OPS-1600 · List physical assets")
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<PhysicalAssetPayload>>> listInventory() {
        List<PhysicalAssetPayload> data = operationsManagementService.listInventory();
        return ResponseEntity.ok(ApiResponses.of("OPS-1600", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "OPS-1610 · Add physical asset")
    @PostMapping("/inventory")
    public ResponseEntity<ApiResponse<PhysicalAssetPayload>> createAsset(
            @Valid @RequestBody CreatePhysicalAssetRequest request
    ) {
        PhysicalAssetPayload data = operationsManagementService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("OPS-1610", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "OPS-1630 · List faculty requests")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FacultyRequestPayload>>> listRequests(
            @RequestParam(required = false) RequestStatus status
    ) {
        List<FacultyRequestPayload> data = operationsManagementService.listRequests(status);
        return ResponseEntity.ok(ApiResponses.of("OPS-1630", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "OPS-1640 · Update request status")
    @PatchMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<FacultyRequestPayload>> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFacultyRequestStatusRequest request
    ) {
        FacultyRequestPayload data = operationsManagementService.updateRequestStatus(id, request);
        return ResponseEntity.ok(ApiResponses.of("OPS-1640", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "OPS-1620 · Infrastructure status")
    @GetMapping("/infrastructure")
    public ResponseEntity<ApiResponse<InfrastructurePayload>> getInfrastructure() {
        InfrastructurePayload data = operationsManagementService.getInfrastructure();
        return ResponseEntity.ok(ApiResponses.of("OPS-1620", LmsStatusCode.SUCCESS, null, data));
    }
}
