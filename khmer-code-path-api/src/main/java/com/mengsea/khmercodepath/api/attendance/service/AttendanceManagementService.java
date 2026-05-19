package com.mengsea.khmercodepath.api.attendance.service;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceSessionPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceStatisticsPayload;
import com.mengsea.khmercodepath.api.attendance.payload.BulkAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.RecordAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.UpdateAttendanceRequest;

import java.util.List;

public interface AttendanceManagementService {

    AttendanceRecordPayload recordAttendance(String sessionId, RecordAttendanceRequest request);

    List<AttendanceRecordPayload> bulkRecordAttendance(String sessionId, BulkAttendanceRequest request);

    AttendanceRecordPayload updateAttendance(Long recordId, UpdateAttendanceRequest request);

    AttendanceSessionPayload getSessionAttendance(String sessionId);

    List<AttendanceRecordPayload> getStudentAttendance(String studentId, Long classId);

    AttendanceStatisticsPayload getStatistics(Long classId, String studentId);
}
