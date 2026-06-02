package com.mengsea.khmercodepath.api.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengsea.khmercodepath.api.quiz.payload.PublishQuizRequest;
import com.mengsea.khmercodepath.api.quiz.payload.QuizAttemptResultDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizQuestionDto;
import com.mengsea.khmercodepath.api.quiz.payload.SubmitAnswersRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.Quiz;
import com.mengsea.khmercodepath.commons.domain.QuizQuestion;
import com.mengsea.khmercodepath.commons.domain.QuizSubmission;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.QuizQuestionRepository;
import com.mengsea.khmercodepath.commons.repository.QuizRepository;
import com.mengsea.khmercodepath.commons.repository.QuizSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        return quizzes.stream().map(q -> toDto(q, List.of(), null)).toList();
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
}
