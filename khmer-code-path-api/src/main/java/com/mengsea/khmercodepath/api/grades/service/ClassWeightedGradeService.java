package com.mengsea.khmercodepath.api.grades.service;

import com.mengsea.khmercodepath.api.grades.GradeLetterUtil;
import com.mengsea.khmercodepath.commons.domain.Assignment;
import com.mengsea.khmercodepath.commons.domain.AssignmentSubmission;
import com.mengsea.khmercodepath.commons.domain.Exam;
import com.mengsea.khmercodepath.commons.domain.ExamSubmission;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.Quiz;
import com.mengsea.khmercodepath.commons.domain.QuizSubmission;
import com.mengsea.khmercodepath.commons.domain.StudentGrade;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.repository.AssignmentRepository;
import com.mengsea.khmercodepath.commons.repository.AssignmentSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.AttendanceRecordRepository;
import com.mengsea.khmercodepath.commons.repository.ExamQuestionRepository;
import com.mengsea.khmercodepath.commons.repository.ExamRepository;
import com.mengsea.khmercodepath.commons.repository.ExamSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.QuizRepository;
import com.mengsea.khmercodepath.commons.repository.QuizSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.StudentGradeRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassWeightedGradeService {

    private final LmsClassRepository lmsClassRepository;
    private final UserRepository userRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Value
    @Builder
    public static class ComponentScores {
        BigDecimal attendance;
        BigDecimal assignment;
        BigDecimal quiz;
        BigDecimal midterm;
        BigDecimal finalExam;
        BigDecimal weightedFinal;

        public boolean hasAnyActivity() {
            return ClassWeightedGradeService.hasAnyActivity(attendance, assignment, quiz, finalExam);
        }
    }

    @Transactional
    public StudentGrade recalculate(Long classId, String studentUuid) {
        LmsClass lmsClass = lmsClassRepository.findById(classId).orElse(null);
        User student = userRepository.findByUuidAndDeletedFalse(studentUuid).orElse(null);
        if (lmsClass == null || student == null) {
            return null;
        }

        ComponentScores components = computeComponents(lmsClass, studentUuid);
        if (!components.hasAnyActivity()) {
            return null;
        }

        StudentGrade entity = studentGradeRepository
                .findFirstByLmsClass_IdAndStudent_UuidOrderByCreatedAtDesc(classId, studentUuid)
                .orElseGet(StudentGrade::new);
        entity.setLmsClass(lmsClass);
        entity.setStudent(student);
        entity.setNumericGrade(components.getWeightedFinal());
        entity.setLetterGrade(GradeLetterUtil.toLetter(components.getWeightedFinal()));
        return studentGradeRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public ComponentScores computeComponents(LmsClass lmsClass, String studentUuid) {
        Long classId = lmsClass.getId();
        BigDecimal attendance = computeAttendancePercent(studentUuid, classId);
        BigDecimal assignment = computeAssignmentPercent(studentUuid, classId);
        BigDecimal quiz = computeQuizPercent(studentUuid, classId);
        BigDecimal finalExam = computeExamPercent(studentUuid, classId);
        BigDecimal midterm = BigDecimal.ZERO;
        BigDecimal weightedFinal = applyWeights(lmsClass, attendance, assignment, quiz, midterm, finalExam);
        return ComponentScores.builder()
                .attendance(attendance)
                .assignment(assignment)
                .quiz(quiz)
                .midterm(midterm)
                .finalExam(finalExam)
                .weightedFinal(weightedFinal)
                .build();
    }

    private BigDecimal applyWeights(
            LmsClass lmsClass,
            BigDecimal attendance,
            BigDecimal assignment,
            BigDecimal quiz,
            BigDecimal midterm,
            BigDecimal finalExam
    ) {
        double weighted =
                nz(attendance) * lmsClass.getWeightAttendance() / 100.0
                        + nz(assignment) * lmsClass.getWeightAssignment() / 100.0
                        + nz(quiz) * lmsClass.getWeightQuiz() / 100.0
                        + nz(midterm) * lmsClass.getWeightMidterm() / 100.0
                        + nz(finalExam) * lmsClass.getWeightFinalExam() / 100.0;
        return BigDecimal.valueOf(weighted).setScale(2, RoundingMode.HALF_UP);
    }

    private static double nz(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private BigDecimal computeAttendancePercent(String studentUuid, Long classId) {
        long total = attendanceRecordRepository.countByStudentUuidAndClass(studentUuid, classId);
        if (total == 0) {
            return null;
        }
        long present = attendanceRecordRepository.countPresentByStudentUuidAndClass(studentUuid, classId);
        return percent(present, total);
    }

    private BigDecimal computeAssignmentPercent(String studentUuid, Long classId) {
        List<Assignment> assignments = assignmentRepository.findPublishedByClassId(classId);
        if (assignments.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int scored = 0;
        for (Assignment assignment : assignments) {
            Optional<AssignmentSubmission> submission = assignmentSubmissionRepository
                    .findByAssignment_IdAndStudent_Uuid(assignment.getId(), studentUuid);
            if (submission.isEmpty()) {
                continue;
            }
            BigDecimal pct = submission.get().getScorePercent();
            if (pct == null) {
                pct = BigDecimal.valueOf(100);
            }
            sum = sum.add(pct);
            scored++;
        }
        if (scored == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(scored), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeExamPercent(String studentUuid, Long classId) {
        List<Exam> exams = examRepository.findPublishedByClassId(classId);
        if (exams.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int scored = 0;
        for (Exam exam : exams) {
            Optional<ExamSubmission> submission = examSubmissionRepository
                    .findByExam_IdAndStudent_Uuid(exam.getId(), studentUuid);
            if (submission.isEmpty()) {
                continue;
            }
            sum = sum.add(examSubmissionPercent(submission.get(), exam));
            scored++;
        }
        if (scored == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(scored), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal examSubmissionPercent(ExamSubmission submission, Exam exam) {
        if ("FAILED".equals(submission.getStatus())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int totalQuestions = examQuestionRepository.findByExam_IdOrderByOrderIndex(exam.getId()).size();
        if (totalQuestions <= 0) {
            totalQuestions = exam.getQuestionCount();
        }
        if (totalQuestions <= 0) {
            return BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        }
        int score = submission.getScore() != null ? submission.getScore() : 0;
        return percent(score, totalQuestions);
    }

    private BigDecimal computeQuizPercent(String studentUuid, Long classId) {
        List<Quiz> quizzes = quizRepository.findPublishedByClassId(classId);
        if (quizzes.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int scored = 0;
        for (Quiz quiz : quizzes) {
            Optional<QuizSubmission> submission = quizSubmissionRepository
                    .findByQuiz_IdAndStudent_Uuid(quiz.getId(), studentUuid);
            if (submission.isEmpty() || !isSubmittedQuizStatus(submission.get().getStatus())) {
                continue;
            }
            int totalQuestions = quiz.getQuestionCount();
            if (totalQuestions <= 0) {
                continue;
            }
            int score = submission.get().getScore() != null ? submission.get().getScore() : 0;
            sum = sum.add(percent(score, totalQuestions));
            scored++;
        }
        if (scored == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(scored), 2, RoundingMode.HALF_UP);
    }

    private static boolean isSubmittedQuizStatus(String status) {
        return "SUBMITTED".equals(status) || "COMPLETED".equals(status);
    }

    private static BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator * 100.0 / denominator)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean hasAnyActivity(ComponentScores scores) {
        return scores.hasAnyActivity();
    }

    public static boolean hasAnyActivity(
            BigDecimal attendance,
            BigDecimal assignment,
            BigDecimal quiz,
            BigDecimal finalExam
    ) {
        return attendance != null || assignment != null || quiz != null || finalExam != null;
    }
}
