package com.mengsea.khmercodepath.api.attendance.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceQualityLevelPayload {
    private String id;
    private String label;
    /** Inclusive minimum attendance rate (0–100) for this band; omitted for no-data band. */
    private Double minRate;
}
