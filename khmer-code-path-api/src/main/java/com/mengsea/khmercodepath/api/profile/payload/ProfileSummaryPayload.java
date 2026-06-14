package com.mengsea.khmercodepath.api.profile.payload;

import com.mengsea.khmercodepath.api.classes.payload.ClassSummaryPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.AdminDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.StudentDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.TeacherDashboardPayload;
import com.mengsea.khmercodepath.api.notes.payload.NoteListPayload;
import com.mengsea.khmercodepath.api.progress.payload.GradeBreakdownPayload;
import com.mengsea.khmercodepath.api.quiz.payload.QuizDto;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ProfileSummaryPayload {
    String role;
    StudentDashboardPayload studentDashboard;
    TeacherDashboardPayload teacherDashboard;
    AdminDashboardPayload adminDashboard;
    NoteListPayload notes;
    List<ClassSummaryPayload> classes;
    List<QuizDto> quizzes;
    List<GradeBreakdownPayload> gradeRows;
}
