package com.mengsea.khmercodepath.api.attendance.service;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceExportResource;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceManagementConfigPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRosterPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceSessionPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceStatisticsPayload;
import com.mengsea.khmercodepath.api.attendance.payload.BulkAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.RecordAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.UpdateAttendanceRequest;

import java.util.List;
import java.time.LocalDate;

public interface AttendanceManagementService {

    AttendanceManagementConfigPayload getManagementConfig(Long classId);

    AttendanceRosterPayload getRoster(Long classId, String search, String monthFilter);

    AttendanceRosterPayload setAttendanceWarning(Long classId, String studentId, boolean warned);

    AttendanceRecordPayload recordAttendance(String sessionId, RecordAttendanceRequest request);

    List<AttendanceRecordPayload> bulkRecordAttendance(String sessionId, BulkAttendanceRequest request);

    AttendanceRecordPayload updateAttendance(Long recordId, UpdateAttendanceRequest request);

    AttendanceSessionPayload getSessionAttendance(String sessionId);

    List<AttendanceRecordPayload> getStudentAttendance(String studentId, Long classId);

    AttendanceStatisticsPayload getStatistics(Long classId, String studentId);

    AttendanceExportResource exportAttendanceExcel(
            Long classId,
            String search,
            String monthFilter
    );
}
