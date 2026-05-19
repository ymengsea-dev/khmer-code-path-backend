package com.mengsea.khmercodepath.api.attendance.payload;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AttendanceSessionPayload {
    private String sessionId;
    private Long classId;
    private LocalDate sessionDate;
    private List<AttendanceRecordPayload> records;
}
