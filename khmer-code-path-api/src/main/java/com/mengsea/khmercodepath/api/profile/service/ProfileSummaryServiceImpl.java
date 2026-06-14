package com.mengsea.khmercodepath.api.profile.service;

import com.mengsea.khmercodepath.api.classes.payload.ClassPagePayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassSummaryPayload;
import com.mengsea.khmercodepath.api.classes.service.ClassManagementService;
import com.mengsea.khmercodepath.api.dashboard.payload.AdminDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.StudentDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.TeacherDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.service.DashboardService;
import com.mengsea.khmercodepath.api.notes.payload.NoteListPayload;
import com.mengsea.khmercodepath.api.notes.service.NoteService;
import com.mengsea.khmercodepath.api.profile.payload.LearningClassPayload;
import com.mengsea.khmercodepath.api.profile.payload.MyLearningPayload;
import com.mengsea.khmercodepath.api.profile.payload.ProfileSummaryPayload;
import com.mengsea.khmercodepath.api.progress.payload.ClassProgressPayload;
import com.mengsea.khmercodepath.api.progress.payload.GradeBreakdownPayload;
import com.mengsea.khmercodepath.api.progress.service.ProgressService;
import com.mengsea.khmercodepath.api.quiz.payload.QuizDto;
import com.mengsea.khmercodepath.api.quiz.service.QuizService;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileSummaryServiceImpl implements ProfileSummaryService {

    private final ClassManagementService classManagementService;
    private final DashboardService dashboardService;
    private final ProgressService progressService;
    private final QuizService quizService;
    private final NoteService noteService;

    @Override
    @Transactional(readOnly = true)
    public MyLearningPayload getMyLearning() {
        User me = SecurityUtils.requireCurrentUser();
        StudentDashboardPayload dashboard = dashboardService.getStudentDashboard();
        ClassPagePayload classPage = classManagementService.listClasses(
                null, null, null, null, null, PageRequest.of(0, 100)
        );
        List<QuizDto> quizzes = quizService.listAssigned();

        List<LearningClassPayload> learningClasses = classPage.getItems().stream()
                .map(klass -> toLearningClass(me.getUuid(), klass, quizzes))
                .toList();

        return MyLearningPayload.builder()
                .dashboard(dashboard)
                .learningClasses(learningClasses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileSummaryPayload getProfileSummary() {
        User me = SecurityUtils.requireCurrentUser();
        Role role = me.getRole();
        NoteListPayload notes = noteService.list(null);

        StudentDashboardPayload studentDashboard = null;
        TeacherDashboardPayload teacherDashboard = null;
        AdminDashboardPayload adminDashboard = null;
        List<ClassSummaryPayload> classes = List.of();
        List<QuizDto> quizzes = List.of();
        List<GradeBreakdownPayload> gradeRows = List.of();

        if (role == Role.STUDENT) {
            studentDashboard = dashboardService.getStudentDashboard();
            classes = classManagementService
                    .listClasses(null, null, null, null, null, PageRequest.of(0, 100))
                    .getItems();
            quizzes = quizService.listAssigned();
            gradeRows = progressService.getGradeBreakdown(me.getUuid());
        } else if (role == Role.TEACHER) {
            teacherDashboard = dashboardService.getTeacherDashboard();
        } else if (role == Role.ADMIN) {
            adminDashboard = dashboardService.getAdminDashboard();
        }

        return ProfileSummaryPayload.builder()
                .role(role.name())
                .studentDashboard(studentDashboard)
                .teacherDashboard(teacherDashboard)
                .adminDashboard(adminDashboard)
                .notes(notes)
                .classes(classes)
                .quizzes(quizzes)
                .gradeRows(gradeRows)
                .build();
    }

    private LearningClassPayload toLearningClass(
            String studentUuid,
            ClassSummaryPayload klass,
            List<QuizDto> quizzes
    ) {
        ClassProgressPayload progress = progressService.getClassProgress(studentUuid, klass.getId());
        long pending = quizzes.stream()
                .filter(q -> klass.getId().equals(q.getClassId()))
                .filter(q -> q.getSubmissionStatus() == null || q.getSubmissionStatus().isBlank())
                .count();
        return LearningClassPayload.builder()
                .summary(klass)
                .progress(progress)
                .pendingQuizzes(pending)
                .build();
    }
}
