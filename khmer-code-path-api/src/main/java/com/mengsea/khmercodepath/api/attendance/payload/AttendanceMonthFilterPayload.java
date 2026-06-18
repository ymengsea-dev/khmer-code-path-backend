package com.mengsea.khmercodepath.api.attendance.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceMonthFilterPayload {
    private String id;
    private String label;
}
