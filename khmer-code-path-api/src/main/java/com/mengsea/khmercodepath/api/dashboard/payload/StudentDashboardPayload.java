package com.mengsea.khmercodepath.api.dashboard.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardPayload {
    private BigDecimal overallGpa;
    private long coursesCompleted;
    private long coursesEnrolled;
    private long quizzesCompleted;
    private double attendanceRate;
}
