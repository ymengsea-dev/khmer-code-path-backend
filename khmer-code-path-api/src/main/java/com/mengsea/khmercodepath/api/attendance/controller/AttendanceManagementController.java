package com.mengsea.khmercodepath.api.attendance.controller;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceManagementConfigPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRosterPayload;
import com.mengsea.khmercodepath.api.attendance.payload.SetAttendanceWarningRequest;
import com.mengsea.khmercodepath.api.attendance.service.AttendanceManagementService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance-management")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Teacher attendance roster and warnings")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class AttendanceManagementController {

    private final AttendanceManagementService attendanceManagementService;

    @Operation(summary = "ATT-0900 · Attendance management UI config")
    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.ATT_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<AttendanceManagementConfigPayload>> getConfig(
            @RequestParam(required = false) Long classId
    ) {
        AttendanceManagementConfigPayload data = attendanceManagementService.getManagementConfig(classId);
        return ResponseEntity.ok(ApiResponses.of("ATT-0900", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ATT-0910 · Class attendance roster")
    @GetMapping("/roster")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.ATT_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<AttendanceRosterPayload>> getRoster(
            @RequestParam Long classId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String month
    ) {
        AttendanceRosterPayload data = attendanceManagementService.getRoster(classId, search, month);
        return ResponseEntity.ok(ApiResponses.of("ATT-0910", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ATT-0920 · Set or clear attendance warning")
    @PatchMapping("/classes/{classId}/students/{studentId}/warning")
    @PreAuthorize("hasAuthority('" + LmsAuthority.ATT_MANAGE + "')")
    public ResponseEntity<ApiResponse<AttendanceRosterPayload>> setWarning(
            @PathVariable Long classId,
            @PathVariable String studentId,
            @Valid @RequestBody SetAttendanceWarningRequest request
    ) {
        AttendanceRosterPayload data = attendanceManagementService.setAttendanceWarning(
                classId,
                studentId,
                Boolean.TRUE.equals(request.getWarned())
        );
        return ResponseEntity.ok(ApiResponses.of("ATT-0920", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ATT-1060 · Export class attendance to Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.ATT_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam Long classId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String month
    ) {
        var resource = attendanceManagementService.exportAttendanceExcel(
                classId,
                search,
                month
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource.getContent());
    }
}
