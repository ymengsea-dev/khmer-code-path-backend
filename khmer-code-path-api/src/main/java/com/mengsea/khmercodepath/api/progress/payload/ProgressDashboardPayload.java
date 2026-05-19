package com.mengsea.khmercodepath.api.progress.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProgressDashboardPayload {
    private String studentId;
    private BigDecimal overallGpa;
    private double attendanceRate;
    private long coursesEnrolled;
    private long coursesCompleted;
    private long quizzesCompleted;
}
