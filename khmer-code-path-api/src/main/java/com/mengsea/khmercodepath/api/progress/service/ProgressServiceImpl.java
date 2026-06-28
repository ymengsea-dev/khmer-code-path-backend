package com.mengsea.khmercodepath.api.progress.service;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.progress.payload.ClassProgressPayload;
import com.mengsea.khmercodepath.api.progress.payload.GradeBreakdownPayload;
import com.mengsea.khmercodepath.api.progress.payload.ProgressDashboardPayload;
import com.mengsea.khmercodepath.api.progress.payload.QuizHistoryPayload;
import com.mengsea.khmercodepath.api.grades.service.ClassWeightedGradeService;
import com.mengsea.khmercodepath.commons.constant.AttendanceStatus;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.AttendanceRecord;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.QuizSubmission;
import com.mengsea.khmercodepath.commons.domain.StudentGrade;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.AttendanceRecordRepository;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.QuizSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.StudentGradeRepository;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final ClassAccessHelper classAccessHelper;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ClassWeightedGradeService classWeightedGradeService;

    @Override
    @Transactional(readOnly = true)
    public ProgressDashboardPayload getDashboard(String studentId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        BigDecimal gpa = studentGradeRepository.averageNumericGradeByStudentUuid(studentId);
        if (gpa != null) {
            gpa = gpa.setScale(2, RoundingMode.HALF_UP);
        }
        long total = attendanceRecordRepository.countByStudentUuid(studentId);
        long present = attendanceRecordRepository.countPresentByStudentUuid(studentId);
        double rate = total == 0
                ? 0.0
                : BigDecimal.valueOf(present * 100.0 / total)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        return ProgressDashboardPayload.builder()
                .studentId(studentId)
                .overallGpa(gpa)
                .attendanceRate(rate)
                .coursesEnrolled(classEnrollmentRepository.countByStudent_Uuid(studentId))
                .coursesCompleted(classEnrollmentRepository.countByStudent_UuidAndCompletedAtIsNotNull(studentId))
                .quizzesCompleted(quizSubmissionRepository.countCompletedByStudentUuid(studentId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassProgressPayload getClassProgress(String studentId, Long classId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        var lmsClass = classAccessHelper.requireReadableClass(classId);
        if (!classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(classId, studentId)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
        StudentGrade grade = studentGradeRepository
                .findFirstByLmsClass_IdAndStudent_UuidOrderByCreatedAtDesc(classId, studentId)
                .orElse(null);
        long attTotal = attendanceRecordRepository.countByStudentUuidAndClass(studentId, classId);
        long attPresent = attendanceRecordRepository.countPresentByStudentUuidAndClass(studentId, classId);
        double attRate = attTotal == 0
                ? 0.0
                : BigDecimal.valueOf(attPresent * 100.0 / attTotal)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        long quizCount = quizSubmissionRepository.findByStudentUuidWithQuiz(studentId).stream()
                .filter(s -> s.getQuiz().getLmsClass().getId().equals(classId))
                .count();
        boolean completed = classEnrollmentRepository.findByStudent_UuidOrderByEnrolledAtDesc(studentId).stream()
                .filter(e -> e.getLmsClass().getId().equals(classId))
                .anyMatch(e -> e.getCompletedAt() != null);
        return ClassProgressPayload.builder()
                .classId(classId)
                .className(lmsClass.getName())
                .classCode(lmsClass.getCode())
                .numericGrade(grade != null ? grade.getNumericGrade() : null)
                .letterGrade(grade != null ? grade.getLetterGrade() : null)
                .attendanceRate(attRate)
                .quizzesCompleted(quizCount)
                .completed(completed)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizHistoryPayload> getQuizHistory(String studentId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        return quizSubmissionRepository.findByStudentUuidWithQuiz(studentId).stream()
                .map(this::toQuizHistory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordPayload> getAttendance(String studentId, Long classId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        if (classId != null) {
            classAccessHelper.requireReadableClass(classId);
        }
        return (classId != null
                ? attendanceRecordRepository.findByStudent_UuidAndLmsClass_IdOrderBySessionDateDesc(
                studentId, classId)
                : attendanceRecordRepository.findByStudent_UuidOrderBySessionDateDesc(studentId))
                .stream()
                .map(this::toAttendancePayload)
                .toList();
    }

    private AttendanceRecordPayload toAttendancePayload(
            com.mengsea.khmercodepath.commons.domain.AttendanceRecord entity
    ) {
        return AttendanceRecordPayload.builder()
                .id(entity.getId())
                .classId(entity.getLmsClass().getId())
                .studentId(entity.getStudent().getUuid())
                .studentName(entity.getStudent().getUsername())
                .sessionDate(entity.getSessionDate())
                .status(AttendanceStatus.valueOf(entity.getStatus()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeBreakdownPayload> getGradeBreakdown(String studentId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        return classEnrollmentRepository.findByStudent_UuidOrderByEnrolledAtDesc(studentId).stream()
                .map(this::toBreakdownRow)
                .toList();
    }

    private GradeBreakdownPayload toBreakdownRow(ClassEnrollment enrollment) {
        Long classId = enrollment.getLmsClass().getId();
        String className = enrollment.getLmsClass().getName();
        String studentUuid = enrollment.getStudent().getUuid();
        ClassWeightedGradeService.ComponentScores components =
                classWeightedGradeService.computeComponents(enrollment.getLmsClass(), studentUuid);
        StudentGrade grade = studentGradeRepository
                .findFirstByLmsClass_IdAndStudent_UuidOrderByCreatedAtDesc(classId, studentUuid)
                .orElse(null);
        BigDecimal numeric = components.getWeightedFinal();
        if (numeric == null && grade != null) {
            numeric = grade.getNumericGrade();
        }
        String letter = grade != null && grade.getLetterGrade() != null
                ? grade.getLetterGrade()
                : (numeric != null ? com.mengsea.khmercodepath.api.grades.GradeLetterUtil.toLetter(numeric) : "—");
        return GradeBreakdownPayload.builder()
                .classId(classId)
                .course(className)
                .assignments(formatComponentPercent(components.getAssignment()))
                .quizzes(formatComponentPercent(components.getQuiz()))
                .midterm("—")
                .finalExam(formatComponentPercent(components.getFinalExam()))
                .attendance(formatComponentPercent(components.getAttendance()))
                .numericGrade(numeric)
                .grade(numeric != null ? letter : "—")
                .build();
    }

    private static String formatComponentPercent(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return value.setScale(0, RoundingMode.HALF_UP) + "%";
    }

    private QuizHistoryPayload toQuizHistory(QuizSubmission submission) {
        return QuizHistoryPayload.builder()
                .submissionId(submission.getId())
                .quizId(submission.getQuiz().getId())
                .quizTitle(submission.getQuiz().getTitle())
                .classId(submission.getQuiz().getLmsClass().getId())
                .className(submission.getQuiz().getLmsClass().getName())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
