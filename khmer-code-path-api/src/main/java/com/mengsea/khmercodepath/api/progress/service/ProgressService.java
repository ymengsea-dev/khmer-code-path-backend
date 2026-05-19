package com.mengsea.khmercodepath.api.progress.service;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.progress.payload.ClassProgressPayload;
import com.mengsea.khmercodepath.api.progress.payload.GradeBreakdownPayload;
import com.mengsea.khmercodepath.api.progress.payload.ProgressDashboardPayload;
import com.mengsea.khmercodepath.api.progress.payload.QuizHistoryPayload;

import java.util.List;

public interface ProgressService {

    ProgressDashboardPayload getDashboard(String studentId);

    ClassProgressPayload getClassProgress(String studentId, Long classId);

    List<QuizHistoryPayload> getQuizHistory(String studentId);

    List<AttendanceRecordPayload> getAttendance(String studentId, Long classId);

    List<GradeBreakdownPayload> getGradeBreakdown(String studentId);
}
