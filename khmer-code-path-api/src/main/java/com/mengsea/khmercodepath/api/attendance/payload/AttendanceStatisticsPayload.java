package com.mengsea.khmercodepath.api.attendance.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceStatisticsPayload {
    private long present;
    private long late;
    private long absent;
    private long total;
    private double attendanceRate;
}
