package com.mengsea.khmercodepath.api.quiz.payload;

import lombok.Builder;
import lombok.Value;

/**
 * Aggregated quiz statistics returned by GET /api/v1/quizzes/summary.
 * <p>
 * Students receive meaningful values for {@code total}, {@code pending},
 * {@code completed}, and {@code missed}; teacher-only fields are 0.
 * Teachers receive meaningful values for {@code total}, {@code totalSubmissions},
 * {@code totalFailed}, and {@code totalQuestions}; student-only fields are 0.
 */
@Value
@Builder
public class QuizSummaryDto {
    /** Total quizzes (assigned for student / published for teacher). */
    long total;
    /** Student: quizzes not yet started. */
    long pending;
    /** Student: quizzes submitted successfully. */
    long completed;
    /** Student: quizzes marked as failed. */
    long missed;
    /** Teacher: total student submissions (SUBMITTED/COMPLETED) across all quizzes. */
    long totalSubmissions;
    /** Teacher: total student failed attempts across all quizzes. */
    long totalFailed;
    /** Teacher: total number of questions across all quizzes. */
    long totalQuestions;
}
