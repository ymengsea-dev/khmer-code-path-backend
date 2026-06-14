package com.mengsea.khmercodepath.api.quiz.service;

import com.mengsea.khmercodepath.api.quiz.payload.PublishQuizRequest;
import com.mengsea.khmercodepath.api.quiz.payload.QuizAttemptResultDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizDto;
import com.mengsea.khmercodepath.api.quiz.payload.QuizResultsDto;
import com.mengsea.khmercodepath.api.quiz.payload.SubmitAnswersRequest;
import com.mengsea.khmercodepath.api.quiz.payload.UpdateQuizRequest;

import java.util.List;

public interface QuizService {

    /** Teacher publishes an AI-generated quiz to a class. */
    QuizDto publish(PublishQuizRequest request);

    /** Teacher: list all quizzes across own classes (or filtered by classId when non-null). */
    List<QuizDto> listForTeacher(Long classId);

    /** Student: list PUBLISHED quizzes for enrolled classes. */
    List<QuizDto> listAssigned();

    /** Get full quiz with questions (students get hidden correct answers). */
    QuizDto getQuiz(Long quizId);

    /** Student submits answers; returns score. */
    QuizAttemptResultDto submit(Long quizId, SubmitAnswersRequest request);

    /** Mark quiz as failed (tab-switch / cheating detection). */
    void fail(Long quizId, String reason);

    /** Teacher soft-deletes a quiz they own. */
    void deleteQuiz(Long quizId);

    /** Teacher reviews quiz submissions and aggregate results. */
    QuizResultsDto getResults(Long quizId);

    /** Teacher edits a quiz before any student attempts exist. */
    QuizDto updateQuiz(Long quizId, UpdateQuizRequest request);
}
