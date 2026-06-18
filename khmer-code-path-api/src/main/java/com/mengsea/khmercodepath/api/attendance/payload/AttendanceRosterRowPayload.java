package com.mengsea.khmercodepath.api.attendance.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRosterRowPayload {
    private String studentId;
    private String studentName;
    private String studentCode;
    private String avatarUrl;
    private long present;
    private long late;
    private long absent;
    private long total;
    private Double attendanceRate;
    private String qualityId;
    private String qualityLabel;
    private boolean warned;
}
