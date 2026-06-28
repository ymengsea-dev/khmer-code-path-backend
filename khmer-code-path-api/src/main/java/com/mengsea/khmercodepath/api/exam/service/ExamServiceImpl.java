package com.mengsea.khmercodepath.api.exam.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengsea.khmercodepath.api.exam.payload.CreateExamRequest;
import com.mengsea.khmercodepath.api.exam.payload.ExamAttemptResultDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamQuestionDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamResultsDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamSubmissionReviewDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamWrongAnswerDto;
import com.mengsea.khmercodepath.api.exam.payload.SubmitExamAnswersRequest;
import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockDto;
import com.mengsea.khmercodepath.api.assignmentsexams.service.TaskContentBlockService;
import com.mengsea.khmercodepath.api.grades.service.ClassWeightedGradeService;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.Exam;
import com.mengsea.khmercodepath.commons.domain.ExamQuestion;
import com.mengsea.khmercodepath.commons.domain.ExamSubmission;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.ExamQuestionRepository;
import com.mengsea.khmercodepath.commons.repository.ExamRepository;
import com.mengsea.khmercodepath.commons.repository.ExamSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final LmsClassRepository lmsClassRepository;
    private final UserRepository userRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final ObjectMapper objectMapper;
    private final TaskContentBlockService taskContentBlockService;
    private final ClassWeightedGradeService classWeightedGradeService;

    @Override
    @Transactional
    public ExamDto create(CreateExamRequest request) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(request.getClassId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));

        if (!lmsClass.getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        taskContentBlockService.validateForTeacher(request.getContentBlocks(), teacherUuid);

        String mergedContent = taskContentBlockService.mergeAiGeneratedContent(
                request.getContentBlocks(), request.getGeneratedContent());
        int questionCount = taskContentBlockService.countQuestions(mergedContent);

        Exam exam = new Exam();
        exam.setLmsClass(lmsClass);
        exam.setTitle(request.getTitle().trim());
        exam.setDescription(request.getDescription());
        exam.setGeneratedContent(mergedContent != null ? mergedContent : "");
        exam.setQuestionCount(questionCount);
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setDueAt(request.getDueAt());
        exam.setContentBlocksJson(taskContentBlockService.serialize(request.getContentBlocks()));
        exam.setStatus("PUBLISHED");
        exam = examRepository.save(exam);

        List<ExamQuestion> questions = parseAndSaveQuestions(exam, mergedContent);
        return toDto(exam, questions, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> listForTeacher(Long classId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();
        List<Exam> exams = (classId != null && classId > 0)
                ? examRepository.findByClassAndTeacher(classId, teacherUuid)
                : examRepository.findAllByTeacherUuid(teacherUuid);
        return exams.stream().map(e -> toDtoWithTeacherCounts(e, null)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDto> listAssigned() {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();
        List<Exam> exams = examRepository.findPublishedForStudent(studentUuid);
        return exams.stream().map(e -> {
            String subStatus = examSubmissionRepository
                    .findByExam_IdAndStudent_Uuid(e.getId(), studentUuid)
                    .map(ExamSubmission::getStatus)
                    .orElse(null);
            return toDto(e, List.of(), subStatus, false);
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDto getExam(Long examId) {
        Exam exam = examRepository.findByIdAndDeletedFalse(examId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.EXAM_NOT_FOUND));

        String currentUuid = SecurityUtils.requireCurrentUser().getUuid();
        boolean isTeacher = exam.getLmsClass().getTeacher().getUuid().equals(currentUuid);

        if (!isTeacher) {
            assertStudentEnrolled(exam, currentUuid);
            if (examSubmissionRepository.findByExam_IdAndStudent_Uuid(examId, currentUuid).isEmpty()) {
                assertBeforeDeadline(exam);
            }
        }

        List<ExamQuestion> questions = examQuestionRepository.findByExam_IdOrderByOrderIndex(examId);

        String subStatus = null;
        if (!isTeacher) {
            subStatus = examSubmissionRepository
                    .findByExam_IdAndStudent_Uuid(examId, currentUuid)
                    .map(ExamSubmission::getStatus)
                    .orElse(null);
        }

        List<ExamQuestionDto> dtos = questions.stream()
                .map(q -> toQuestionDto(q, isTeacher))
                .toList();

        return toDto(exam, questions, subStatus, isTeacher).toBuilder()
                .questions(dtos)
                .generatedContent(isTeacher ? exam.getGeneratedContent() : null)
                .build();
    }

    @Override
    @Transactional
    public ExamAttemptResultDto submit(Long examId, SubmitExamAnswersRequest request) {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();

        Exam exam = examRepository.findByIdAndDeletedFalse(examId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.EXAM_NOT_FOUND));

        assertStudentEnrolled(exam, studentUuid);
        assertBeforeDeadline(exam);

        if (examSubmissionRepository.existsByExam_IdAndStudent_Uuid(examId, studentUuid)) {
            throw new BusinessException(ExceptionCode.EXAM_ALREADY_SUBMITTED);
        }

        User student = userRepository.findByUuidAndDeletedFalse(studentUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        List<ExamQuestion> questions = examQuestionRepository.findByExam_IdOrderByOrderIndex(examId);

        Map<Long, Integer> answers = request.getAnswers();
        int correct = 0;
        for (ExamQuestion q : questions) {
            Integer selected = answers.get(q.getId());
            if (selected != null && selected == q.getCorrectIndex()) {
                correct++;
            }
        }

        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            answersJson = "{}";
        }

        ExamSubmission submission = new ExamSubmission();
        submission.setExam(exam);
        submission.setStudent(student);
        submission.setScore(correct);
        submission.setAnswersJson(answersJson);
        submission.setStatus("SUBMITTED");
        submission = examSubmissionRepository.save(submission);

        classWeightedGradeService.recalculate(exam.getLmsClass().getId(), studentUuid);

        int totalQuestions = questions.isEmpty() ? exam.getQuestionCount() : questions.size();
        BigDecimal scorePercent = classWeightedGradeService.examSubmissionPercent(submission, exam);

        return ExamAttemptResultDto.builder()
                .examId(examId)
                .score(correct)
                .totalQuestions(totalQuestions)
                .scorePercent(scorePercent)
                .status("SUBMITTED")
                .failReason(null)
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    @Override
    @Transactional
    public void fail(Long examId, String reason) {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();

        Exam exam = examRepository.findByIdAndDeletedFalse(examId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.EXAM_NOT_FOUND));

        assertStudentEnrolled(exam, studentUuid);

        examSubmissionRepository.findByExam_IdAndStudent_Uuid(examId, studentUuid)
                .ifPresentOrElse(
                        sub -> { /* already recorded */ },
                        () -> {
                            User student = userRepository.findByUuidAndDeletedFalse(studentUuid)
                                    .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
                            ExamSubmission sub = new ExamSubmission();
                            sub.setExam(exam);
                            sub.setStudent(student);
                            sub.setStatus("FAILED");
                            sub.setFailReason(reason);
                            examSubmissionRepository.save(sub);
                            classWeightedGradeService.recalculate(exam.getLmsClass().getId(), studentUuid);
                        }
                );
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResultsDto getResults(Long examId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        Exam exam = examRepository.findByIdAndDeletedFalse(examId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.EXAM_NOT_FOUND));

        if (!exam.getLmsClass().getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        List<ExamQuestion> questions = examQuestionRepository.findByExam_IdOrderByOrderIndex(examId);
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamIdWithStudent(examId);
        int totalQuestions = questions.isEmpty() ? exam.getQuestionCount() : questions.size();

        List<ExamSubmissionReviewDto> rows = submissions.stream()
                .map(sub -> toReviewDto(sub, totalQuestions, questions))
                .toList();

        List<Integer> submittedScores = submissions.stream()
                .filter(sub -> isSubmittedStatus(sub.getStatus()))
                .map(ExamSubmission::getScore)
                .filter(score -> score != null)
                .toList();

        long enrolled = classEnrollmentRepository.countByLmsClass_Id(exam.getLmsClass().getId());
        long failed = submissions.stream().filter(sub -> "FAILED".equals(sub.getStatus())).count();
        double averagePercent = submittedScores.isEmpty() || totalQuestions == 0
                ? 0.0
                : submittedScores.stream().mapToInt(Integer::intValue).average().orElse(0.0) * 100.0 / totalQuestions;

        ExamDto examDto = toDtoWithTeacherCounts(exam, null);

        return ExamResultsDto.builder()
                .exam(examDto)
                .enrolledStudents(enrolled)
                .submittedCount(submittedScores.size())
                .failedCount(failed)
                .notStartedCount(Math.max(0, enrolled - submissions.size()))
                .averageScorePercent(round1(averagePercent))
                .highestScore(submittedScores.stream().mapToInt(Integer::intValue).max().orElse(0))
                .lowestScore(submittedScores.stream().mapToInt(Integer::intValue).min().orElse(0))
                .submissions(rows)
                .build();
    }

    @Override
    @Transactional
    public void deleteExam(Long examId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        Exam exam = examRepository.findByIdAndDeletedFalse(examId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.EXAM_NOT_FOUND));

        if (!exam.getLmsClass().getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        exam.setDeleted(true);
        examRepository.save(exam);
    }

    private List<ExamQuestion> parseAndSaveQuestions(Exam exam, String generatedContent) {
        List<ExamQuestion> saved = new ArrayList<>();
        if (generatedContent == null || generatedContent.isBlank()) {
            return saved;
        }
        try {
            String cleaned = generatedContent
                    .replaceAll("(?m)^```[a-zA-Z]*$", "")
                    .replaceAll("(?m)^```$", "")
                    .trim();

            List<Map<String, Object>> parsed = objectMapper.readValue(
                    cleaned, new TypeReference<>() {});

            for (int i = 0; i < parsed.size(); i++) {
                Map<String, Object> item = parsed.get(i);
                ExamQuestion q = new ExamQuestion();
                q.setExam(exam);
                q.setOrderIndex(i);
                q.setQuestionText(String.valueOf(item.getOrDefault("question", "")));
                q.setCorrectIndex(((Number) item.getOrDefault("correctIndex", 0)).intValue());
                q.setExplanation((String) item.get("explanation"));

                Object optionsRaw = item.get("options");
                String optionsJson = objectMapper.writeValueAsString(optionsRaw);
                q.setOptionsJson(optionsJson);

                saved.add(examQuestionRepository.save(q));
            }
        } catch (Exception e) {
            log.warn("Could not parse exam questions from generated content: {}", e.getMessage());
        }
        return saved;
    }

    private ExamDto toDto(Exam exam, List<ExamQuestion> questions, String submissionStatus, boolean includeAiAnswers) {
        List<TaskContentBlockDto> blocks = taskContentBlockService.enrichBlocks(
                exam.getContentBlocksJson(),
                null,
                exam.getId(),
                includeAiAnswers
        );
        return ExamDto.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .classId(exam.getLmsClass().getId())
                .className(exam.getLmsClass().getName())
                .questionCount(exam.getQuestionCount())
                .durationMinutes(exam.getDurationMinutes())
                .status(exam.getStatus())
                .createdAt(exam.getCreatedAt())
                .dueAt(exam.getDueAt())
                .pastDue(isPastDue(exam))
                .questions(null)
                .submissionStatus(submissionStatus)
                .contentBlocks(blocks.isEmpty() ? null : blocks)
                .build();
    }

    private ExamDto toDtoWithTeacherCounts(Exam exam, String submissionStatus) {
        long enrolled = classEnrollmentRepository.countByLmsClass_Id(exam.getLmsClass().getId());
        long submitted = examSubmissionRepository.countSubmittedByExamId(exam.getId());
        long failed = examSubmissionRepository.countByExam_IdAndStatus(exam.getId(), "FAILED");
        return toDto(exam, List.of(), submissionStatus, true).toBuilder()
                .enrolledStudents(enrolled)
                .submittedCount(submitted)
                .failedCount(failed)
                .build();
    }

    private ExamQuestionDto toQuestionDto(ExamQuestion q, boolean includeAnswer) {
        List<String> options;
        try {
            options = objectMapper.readValue(q.getOptionsJson(), new TypeReference<>() {});
        } catch (Exception e) {
            options = List.of();
        }
        return ExamQuestionDto.builder()
                .id(q.getId())
                .question(q.getQuestionText())
                .options(options)
                .correctIndex(includeAnswer ? q.getCorrectIndex() : null)
                .explanation(includeAnswer ? q.getExplanation() : null)
                .build();
    }

    private ExamSubmissionReviewDto toReviewDto(
            ExamSubmission submission,
            int totalQuestions,
            List<ExamQuestion> questions
    ) {
        Integer score = submission.getScore();
        Double scorePercent = score == null || totalQuestions == 0
                ? null
                : round1(score * 100.0 / totalQuestions);
        Map<Long, Integer> answers = parseAnswers(submission.getAnswersJson());
        return ExamSubmissionReviewDto.builder()
                .submissionId(submission.getId())
                .studentId(submission.getStudent().getUuid())
                .studentName(submission.getStudent().getUsername())
                .studentEmail(submission.getStudent().getEmail())
                .status(submission.getStatus())
                .score(score)
                .totalQuestions(totalQuestions)
                .scorePercent(scorePercent)
                .failReason(submission.getFailReason())
                .answers(answers)
                .wrongAnswers(buildWrongAnswers(questions, answers))
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    private List<ExamWrongAnswerDto> buildWrongAnswers(
            List<ExamQuestion> questions,
            Map<Long, Integer> answers
    ) {
        if (questions.isEmpty()) {
            return List.of();
        }
        List<ExamWrongAnswerDto> wrong = new ArrayList<>();
        for (ExamQuestion question : questions) {
            Integer selected = answers.get(question.getId());
            if (selected != null && selected == question.getCorrectIndex()) {
                continue;
            }
            List<String> options = parseOptions(question.getOptionsJson());
            wrong.add(ExamWrongAnswerDto.builder()
                    .questionId(question.getId())
                    .question(question.getQuestionText())
                    .selectedIndex(selected)
                    .selectedAnswer(optionAt(options, selected))
                    .correctIndex(question.getCorrectIndex())
                    .correctAnswer(optionAt(options, question.getCorrectIndex()))
                    .explanation(question.getExplanation())
                    .build());
        }
        return wrong;
    }

    private void assertStudentEnrolled(Exam exam, String studentUuid) {
        if (!classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(
                exam.getLmsClass().getId(), studentUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
    }

    private void assertBeforeDeadline(Exam exam) {
        if (isPastDue(exam)) {
            throw new BusinessException(ExceptionCode.EXAM_DEADLINE_PASSED);
        }
    }

    private static boolean isPastDue(Exam exam) {
        return exam.getDueAt() != null && LocalDateTime.now().isAfter(exam.getDueAt());
    }

    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static String optionAt(List<String> options, Integer index) {
        if (index == null || index < 0 || index >= options.size()) {
            return null;
        }
        return options.get(index);
    }

    private Map<Long, Integer> parseAnswers(String answersJson) {
        if (answersJson == null || answersJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(answersJson, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static boolean isSubmittedStatus(String status) {
        return "SUBMITTED".equals(status) || "COMPLETED".equals(status);
    }
}
