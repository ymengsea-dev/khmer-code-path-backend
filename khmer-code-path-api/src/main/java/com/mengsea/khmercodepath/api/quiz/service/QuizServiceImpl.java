package com.mengsea.khmercodepath.api.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengsea.khmercodepath.api.quiz.payload.PublishQuizRequest;
import com.mengsea.khmercodepath.api.quiz.payload.QuizAttemptResultDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizQuestionDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizResultsDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizSubmissionReviewDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizSummaryDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizWrongAnswerDto;
import com.mengsea.khmercodepath.api.quiz.payload.SubmitAnswersRequest;
import com.mengsea.khmercodepath.api.quiz.payload.UpdateQuizRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.Quiz;
import com.mengsea.khmercodepath.commons.domain.QuizQuestion;
import com.mengsea.khmercodepath.commons.domain.QuizSubmission;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.QuizQuestionRepository;
import com.mengsea.khmercodepath.commons.repository.QuizRepository;
import com.mengsea.khmercodepath.commons.repository.QuizSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final LmsClassRepository lmsClassRepository;
    private final UserRepository userRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final ObjectMapper objectMapper;

    // ── Publish ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public QuizDto publish(PublishQuizRequest request) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(request.getClassId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));

        // Only the class teacher (or an admin) may publish to it
        if (!lmsClass.getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        Quiz quiz = new Quiz();
        quiz.setLmsClass(lmsClass);
        quiz.setTitle(request.getTitle().trim());
        quiz.setDescription(request.getDescription());
        quiz.setGeneratedContent(request.getGeneratedContent());
        quiz.setQuestionCount(request.getQuestionCount());
        quiz.setDurationMinutes(request.getDurationMinutes());
        quiz.setStatus("PUBLISHED");
        quiz = quizRepository.save(quiz);

        // Parse and persist individual questions
        List<QuizQuestion> questions = parseAndSaveQuestions(quiz, request.getGeneratedContent());

        return toDto(quiz, questions, null);
    }

    // ── List ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> listForTeacher(Long classId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();
        List<Quiz> quizzes = (classId != null && classId > 0)
                ? quizRepository.findByClassAndTeacher(classId, teacherUuid)
                : quizRepository.findAllByTeacherUuid(teacherUuid);
        return quizzes.stream().map(q -> toDtoWithTeacherCounts(q, null)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> listAssigned() {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();
        List<Quiz> quizzes = quizRepository.findPublishedForStudent(studentUuid);
        return quizzes.stream().map(q -> {
            String subStatus = quizSubmissionRepository
                    .findByQuiz_IdAndStudent_Uuid(q.getId(), studentUuid)
                    .map(QuizSubmission::getStatus)
                    .orElse(null);
            return toDto(q, List.of(), subStatus);
        }).toList();
    }

    // ── Get (with questions) ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public QuizDto getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findByIdAndDeletedFalse(quizId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.QUIZ_NOT_FOUND));

        String currentUuid = SecurityUtils.requireCurrentUser().getUuid();
        boolean isTeacher = quiz.getLmsClass().getTeacher().getUuid().equals(currentUuid);

        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz_IdOrderByOrderIndex(quizId);

        String subStatus = null;
        if (!isTeacher) {
            subStatus = quizSubmissionRepository
                    .findByQuiz_IdAndStudent_Uuid(quizId, currentUuid)
                    .map(QuizSubmission::getStatus)
                    .orElse(null);
        }

        List<QuizQuestionDto> dtos = questions.stream()
                .map(q -> toQuestionDto(q, isTeacher))
                .toList();

        return toDto(quiz, questions, subStatus).toBuilder()
                .questions(dtos)
                .generatedContent(isTeacher ? quiz.getGeneratedContent() : null)
                .build();
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public QuizAttemptResultDto submit(Long quizId, SubmitAnswersRequest request) {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();

        Quiz quiz = quizRepository.findByIdAndDeletedFalse(quizId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.QUIZ_NOT_FOUND));

        if (quizSubmissionRepository.existsByQuiz_IdAndStudent_Uuid(quizId, studentUuid)) {
            throw new BusinessException(ExceptionCode.QUIZ_ALREADY_SUBMITTED);
        }

        User student = userRepository.findByUuidAndDeletedFalse(studentUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz_IdOrderByOrderIndex(quizId);

        // Score the answers
        Map<Long, Integer> answers = request.getAnswers();
        int correct = 0;
        for (QuizQuestion q : questions) {
            Integer selected = answers.get(q.getId());
            if (selected != null && selected == q.getCorrectIndex()) correct++;
        }

        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            answersJson = "{}";
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setScore(correct);
        submission.setAnswersJson(answersJson);
        submission.setStatus("SUBMITTED");
        submission = quizSubmissionRepository.save(submission);

        return QuizAttemptResultDto.builder()
                .quizId(quizId)
                .score(correct)
                .totalQuestions(questions.size())
                .status("SUBMITTED")
                .failReason(null)
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    // ── Fail ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void fail(Long quizId, String reason) {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();

        Quiz quiz = quizRepository.findByIdAndDeletedFalse(quizId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.QUIZ_NOT_FOUND));

        // Upsert: if already failed/submitted, ignore
        quizSubmissionRepository.findByQuiz_IdAndStudent_Uuid(quizId, studentUuid)
                .ifPresentOrElse(
                        sub -> { /* already recorded — do nothing */ },
                        () -> {
                            User student = userRepository.findByUuidAndDeletedFalse(studentUuid)
                                    .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
                            QuizSubmission sub = new QuizSubmission();
                            sub.setQuiz(quiz);
                            sub.setStudent(student);
                            sub.setStatus("FAILED");
                            sub.setFailReason(reason);
                            quizSubmissionRepository.save(sub);
                        }
                );
    }

    // ── Results ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public QuizResultsDto getResults(Long quizId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        Quiz quiz = quizRepository.findByIdAndDeletedFalse(quizId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.QUIZ_NOT_FOUND));

        if (!quiz.getLmsClass().getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz_IdOrderByOrderIndex(quizId);
        List<QuizSubmission> submissions = quizSubmissionRepository.findByQuizIdWithStudent(quizId);
        int totalQuestions = questions.isEmpty() ? quiz.getQuestionCount() : questions.size();

        List<QuizSubmissionReviewDto> rows = submissions.stream()
                .map(sub -> toReviewDto(sub, totalQuestions, questions))
                .toList();

        List<Integer> submittedScores = submissions.stream()
                .filter(sub -> isSubmittedStatus(sub.getStatus()))
                .map(QuizSubmission::getScore)
                .filter(score -> score != null)
                .toList();

        long enrolled = classEnrollmentRepository.countByLmsClass_Id(quiz.getLmsClass().getId());
        long failed = submissions.stream().filter(sub -> "FAILED".equals(sub.getStatus())).count();
        double averagePercent = submittedScores.isEmpty() || totalQuestions == 0
                ? 0.0
                : submittedScores.stream().mapToInt(Integer::intValue).average().orElse(0.0) * 100.0 / totalQuestions;

        QuizDto quizDto = toDto(quiz, List.of(), null);

        return QuizResultsDto.builder()
                .quiz(quizDto.toBuilder()
                        .enrolledStudents(enrolled)
                        .submittedCount((long) submittedScores.size())
                        .failedCount(failed)
                        .build())
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

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public QuizDto updateQuiz(Long quizId, UpdateQuizRequest request) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        Quiz quiz = quizRepository.findByIdAndDeletedFalse(quizId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.QUIZ_NOT_FOUND));

        if (!quiz.getLmsClass().getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        if (quizSubmissionRepository.countByQuiz_Id(quizId) > 0) {
            throw new BusinessException(ExceptionCode.QUIZ_HAS_SUBMISSIONS);
        }

        quiz.setTitle(request.getTitle().trim());
        quiz.setDescription(request.getDescription());
        quiz.setGeneratedContent(request.getGeneratedContent());
        quiz.setQuestionCount(request.getQuestionCount());
        quiz.setDurationMinutes(request.getDurationMinutes());
        quiz = quizRepository.save(quiz);

        quizQuestionRepository.deleteByQuiz_Id(quizId);
        List<QuizQuestion> questions = parseAndSaveQuestions(quiz, request.getGeneratedContent());

        return toDtoWithTeacherCounts(quiz, null).toBuilder()
                .questions(questions.stream().map(q -> toQuestionDto(q, true)).toList())
                .generatedContent(quiz.getGeneratedContent())
                .build();
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public QuizSummaryDto getSummary() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> LmsAuthority.LSN_MANAGE.equals(a.getAuthority()));

        String userUuid = SecurityUtils.requireCurrentUser().getUuid();

        if (isTeacher) {
            long total = quizRepository.countByTeacherUuid(userUuid);
            long totalSubmissions = quizSubmissionRepository.countSubmittedByTeacherUuid(userUuid);
            long totalFailed = quizSubmissionRepository.countFailedByTeacherUuid(userUuid);
            long totalQuestions = quizRepository.sumQuestionCountByTeacherUuid(userUuid);
            return QuizSummaryDto.builder()
                    .total(total)
                    .pending(0)
                    .completed(0)
                    .missed(0)
                    .totalSubmissions(totalSubmissions)
                    .totalFailed(totalFailed)
                    .totalQuestions(totalQuestions)
                    .build();
        } else {
            long total = quizRepository.countPublishedForStudent(userUuid);
            long completed = quizSubmissionRepository.countByStudent_UuidAndStatus(userUuid, "SUBMITTED");
            long missed = quizSubmissionRepository.countByStudent_UuidAndStatus(userUuid, "FAILED");
            long pending = Math.max(0L, total - completed - missed);
            return QuizSummaryDto.builder()
                    .total(total)
                    .pending(pending)
                    .completed(completed)
                    .missed(missed)
                    .totalSubmissions(0)
                    .totalFailed(0)
                    .totalQuestions(0)
                    .build();
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteQuiz(Long quizId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        Quiz quiz = quizRepository.findByIdAndDeletedFalse(quizId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.QUIZ_NOT_FOUND));

        if (!quiz.getLmsClass().getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        quiz.setDeleted(true);
        quizRepository.save(quiz);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<QuizQuestion> parseAndSaveQuestions(Quiz quiz, String generatedContent) {
        List<QuizQuestion> saved = new ArrayList<>();
        try {
            // Strip potential markdown code fences
            String cleaned = generatedContent
                    .replaceAll("(?m)^```[a-zA-Z]*$", "")
                    .replaceAll("(?m)^```$", "")
                    .trim();

            List<Map<String, Object>> parsed = objectMapper.readValue(
                    cleaned, new TypeReference<>() {});

            for (int i = 0; i < parsed.size(); i++) {
                Map<String, Object> item = parsed.get(i);
                QuizQuestion q = new QuizQuestion();
                q.setQuiz(quiz);
                q.setOrderIndex(i);
                q.setQuestionText(String.valueOf(item.getOrDefault("question", "")));
                q.setCorrectIndex(((Number) item.getOrDefault("correctIndex", 0)).intValue());
                q.setExplanation((String) item.get("explanation"));

                Object optionsRaw = item.get("options");
                String optionsJson = objectMapper.writeValueAsString(optionsRaw);
                q.setOptionsJson(optionsJson);

                saved.add(quizQuestionRepository.save(q));
            }
        } catch (Exception e) {
            log.warn("Could not parse quiz questions from generated content: {}", e.getMessage());
        }
        return saved;
    }

    private QuizDto toDto(Quiz quiz, List<QuizQuestion> questions, String submissionStatus) {
        return QuizDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .classId(quiz.getLmsClass().getId())
                .className(quiz.getLmsClass().getName())
                .questionCount(quiz.getQuestionCount())
                .durationMinutes(quiz.getDurationMinutes())
                .status(quiz.getStatus())
                .createdAt(quiz.getCreatedAt())
                .dueAt(quiz.getDueAt())
                .questions(null)
                .submissionStatus(submissionStatus)
                .build();
    }

    private QuizDto toDtoWithTeacherCounts(Quiz quiz, String submissionStatus) {
        long enrolled = classEnrollmentRepository.countByLmsClass_Id(quiz.getLmsClass().getId());
        long submitted = quizSubmissionRepository.countSubmittedByQuizId(quiz.getId());
        long failed = quizSubmissionRepository.countByQuiz_IdAndStatus(quiz.getId(), "FAILED");
        return toDto(quiz, List.of(), submissionStatus).toBuilder()
                .enrolledStudents(enrolled)
                .submittedCount(submitted)
                .failedCount(failed)
                .build();
    }

    private QuizQuestionDto toQuestionDto(QuizQuestion q, boolean includeAnswer) {
        List<String> options;
        try {
            options = objectMapper.readValue(q.getOptionsJson(), new TypeReference<>() {});
        } catch (Exception e) {
            options = List.of();
        }
        return QuizQuestionDto.builder()
                .id(q.getId())
                .question(q.getQuestionText())
                .options(options)
                .correctIndex(includeAnswer ? q.getCorrectIndex() : null)
                .explanation(includeAnswer ? q.getExplanation() : null)
                .build();
    }

    private QuizSubmissionReviewDto toReviewDto(
            QuizSubmission submission,
            int totalQuestions,
            List<QuizQuestion> questions
    ) {
        Integer score = submission.getScore();
        Double scorePercent = score == null || totalQuestions == 0
                ? null
                : round1(score * 100.0 / totalQuestions);
        Map<Long, Integer> answers = parseAnswers(submission.getAnswersJson());
        return QuizSubmissionReviewDto.builder()
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

    private List<QuizWrongAnswerDto> buildWrongAnswers(
            List<QuizQuestion> questions,
            Map<Long, Integer> answers
    ) {
        if (questions.isEmpty()) {
            return List.of();
        }
        List<QuizWrongAnswerDto> wrong = new ArrayList<>();
        for (QuizQuestion question : questions) {
            Integer selected = answers.get(question.getId());
            if (selected != null && selected == question.getCorrectIndex()) {
                continue;
            }
            List<String> options = parseOptions(question.getOptionsJson());
            wrong.add(QuizWrongAnswerDto.builder()
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
