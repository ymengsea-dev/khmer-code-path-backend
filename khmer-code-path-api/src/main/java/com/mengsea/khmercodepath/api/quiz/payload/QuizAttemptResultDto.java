package com.mengsea.khmercodepath.api.quiz.payload;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class QuizAttemptResultDto {
    Long quizId;
    Integer score;
    int totalQuestions;
    /** SUBMITTED | FAILED */
    String status;
    String failReason;
    LocalDateTime submittedAt;
}
