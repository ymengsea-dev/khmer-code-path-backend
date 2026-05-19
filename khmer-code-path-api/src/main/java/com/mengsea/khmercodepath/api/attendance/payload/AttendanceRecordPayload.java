package com.mengsea.khmercodepath.api.attendance.payload;

import com.mengsea.khmercodepath.commons.constant.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AttendanceRecordPayload {
    private Long id;
    private Long classId;
    private String studentId;
    private String studentName;
    private LocalDate sessionDate;
    private AttendanceStatus status;
}
