package com.mengsea.khmercodepath.api.attendance.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRosterPayload {
    private Long classId;
    private String className;
    private List<AttendanceRosterRowPayload> rows;
    private long warnedCount;
    private Double classAverageRate;
}
