package com.mengsea.khmercodepath.api.dashboard.service;

import com.mengsea.khmercodepath.api.dashboard.payload.AdminDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.StudentDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.TeacherDashboardPayload;

public interface DashboardService {

    StudentDashboardPayload getStudentDashboard();

    TeacherDashboardPayload getTeacherDashboard();

    AdminDashboardPayload getAdminDashboard();
}
