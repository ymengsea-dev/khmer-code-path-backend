package com.mengsea.khmercodepath.api.attendance.controller;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceSessionPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceStatisticsPayload;
import com.mengsea.khmercodepath.api.attendance.payload.BulkAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.RecordAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.UpdateAttendanceRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "ATT — attendance records (V3 tables)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class AttendanceManagementController {

    private final AttendanceManagementService attendanceManagementService;

    @Operation(summary = "ATT-1000 · Record attendance")
    @PostMapping("/sessions/{sessionId}/records")
    @PreAuthorize("hasAuthority('" + LmsAuthority.ATT_MANAGE + "')")
    public ResponseEntity<ApiResponse<AttendanceRecordPayload>> recordAttendance(
            @PathVariable String sessionId,
            @Valid @RequestBody RecordAttendanceRequest request
    ) {
        AttendanceRecordPayload data = attendanceManagementService.recordAttendance(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("ATT-1000", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "ATT-1010 · Bulk record attendance")
    @PostMapping("/sessions/{sessionId}/records/bulk")
    @PreAuthorize("hasAuthority('" + LmsAuthority.ATT_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordPayload>>> bulkRecord(
            @PathVariable String sessionId,
            @Valid @RequestBody BulkAttendanceRequest request
    ) {
        List<AttendanceRecordPayload> data =
                attendanceManagementService.bulkRecordAttendance(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("ATT-1010", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "ATT-1020 · Update attendance record")
    @PutMapping("/records/{recordId}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.ATT_MANAGE + "')")
    public ResponseEntity<ApiResponse<AttendanceRecordPayload>> updateAttendance(
            @PathVariable Long recordId,
            @Valid @RequestBody UpdateAttendanceRequest request
    ) {
        AttendanceRecordPayload data = attendanceManagementService.updateAttendance(recordId, request);
        return ResponseEntity.ok(ApiResponses.of("ATT-1020", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ATT-1030 · Attendance by session")
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<AttendanceSessionPayload>> getSession(
            @PathVariable String sessionId
    ) {
        AttendanceSessionPayload data = attendanceManagementService.getSessionAttendance(sessionId);
        return ResponseEntity.ok(ApiResponses.of("ATT-1030", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ATT-1040 · Attendance by student")
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordPayload>>> getByStudent(
            @PathVariable String studentId,
            @RequestParam(required = false) Long classId
    ) {
        List<AttendanceRecordPayload> data =
                attendanceManagementService.getStudentAttendance(studentId, classId);
        return ResponseEntity.ok(ApiResponses.of("ATT-1040", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "ATT-1050 · Attendance statistics")
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<AttendanceStatisticsPayload>> statistics(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String studentId
    ) {
        AttendanceStatisticsPayload data =
                attendanceManagementService.getStatistics(classId, studentId);
        return ResponseEntity.ok(ApiResponses.of("ATT-1050", LmsStatusCode.SUCCESS, null, data));
    }
}
