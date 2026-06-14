package com.mengsea.khmercodepath.api.quiz.payload;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class QuizSubmissionReviewDto {
    Long submissionId;
    String studentId;
    String studentName;
    String studentEmail;
    String status;
    Integer score;
    Integer totalQuestions;
    Double scorePercent;
    String failReason;
    Map<Long, Integer> answers;
    List<QuizWrongAnswerDto> wrongAnswers;
    LocalDateTime submittedAt;
}
