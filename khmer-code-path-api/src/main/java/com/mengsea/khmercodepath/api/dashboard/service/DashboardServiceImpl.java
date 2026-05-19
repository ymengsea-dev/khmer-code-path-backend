package com.mengsea.khmercodepath.api.dashboard.service;

import com.mengsea.khmercodepath.api.classes.payload.ClassCommentPayload;
import com.mengsea.khmercodepath.api.classes.service.ClassCommentService;
import com.mengsea.khmercodepath.api.dashboard.payload.AdminDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.StudentDashboardPayload;
import com.mengsea.khmercodepath.api.dashboard.payload.TeacherDashboardPayload;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.AttendanceRecordRepository;
import com.mengsea.khmercodepath.commons.repository.ClassCommentRepository;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.DepartmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.QuizRepository;
import com.mengsea.khmercodepath.commons.repository.QuizSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.StudentGradeRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LmsClassRepository lmsClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final ClassCommentRepository classCommentRepository;
    private final ClassCommentService classCommentService;

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardPayload getStudentDashboard() {
        User me = requireRole(Role.STUDENT);
        String studentUuid = me.getUuid();

        long enrolled = classEnrollmentRepository.countByStudent_Uuid(studentUuid);
        long completed = classEnrollmentRepository.countByStudent_UuidAndCompletedAtIsNotNull(studentUuid);
        long quizzesCompleted = quizSubmissionRepository.countCompletedByStudentUuid(studentUuid);

        long attendanceTotal = attendanceRecordRepository.countByStudentUuid(studentUuid);
        long attendancePresent = attendanceRecordRepository.countPresentByStudentUuid(studentUuid);
        double attendanceRate = attendanceTotal == 0
                ? 0.0
                : round2(attendancePresent * 100.0 / attendanceTotal);

        BigDecimal gpa = studentGradeRepository.averageNumericGradeByStudentUuid(studentUuid);
        if (gpa != null) {
            gpa = gpa.setScale(2, RoundingMode.HALF_UP);
        }

        return StudentDashboardPayload.builder()
                .overallGpa(gpa)
                .coursesCompleted(completed)
                .coursesEnrolled(enrolled)
                .quizzesCompleted(quizzesCompleted)
                .attendanceRate(attendanceRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDashboardPayload getTeacherDashboard() {
        User me = requireRole(Role.TEACHER);
        String teacherUuid = me.getUuid();

        long activeClasses = lmsClassRepository.countByTeacher_UuidAndDeletedFalseAndStatus(
                teacherUuid,
                ClassStatus.ACTIVE
        );
        long quizzes = quizRepository.countByTeacherUuid(teacherUuid);
        long students = classEnrollmentRepository.countStudentsByTeacherUuid(teacherUuid);
        long studentQuestions = classCommentRepository.countByTeacherUuid(teacherUuid);

        List<ClassCommentPayload> recentQuestions = classCommentRepository
                .findRecentByTeacherUuid(teacherUuid, PageRequest.of(0, 8))
                .stream()
                .map(classCommentService::toPayload)
                .toList();

        return TeacherDashboardPayload.builder()
                .activeClasses(activeClasses)
                .quizzes(quizzes)
                .students(students)
                .studentQuestions(studentQuestions)
                .recentQuestions(recentQuestions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardPayload getAdminDashboard() {
        requireRole(Role.ADMIN);
        return AdminDashboardPayload.builder()
                .totalStudents(userRepository.countByRoleAndDeletedFalse(Role.STUDENT))
                .totalInstructors(userRepository.countByRoleAndDeletedFalse(Role.TEACHER))
                .totalDepartments(departmentRepository.countByDeletedFalse())
                .totalClasses(lmsClassRepository.countByDeletedFalse())
                .build();
    }

    private User requireRole(Role role) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != role) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
        return me;
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
