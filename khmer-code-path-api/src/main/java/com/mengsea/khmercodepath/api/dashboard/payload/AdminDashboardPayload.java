package com.mengsea.khmercodepath.api.dashboard.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardPayload {
    private long totalStudents;
    private long totalInstructors;
    private long totalDepartments;
    private long totalClasses;
}
