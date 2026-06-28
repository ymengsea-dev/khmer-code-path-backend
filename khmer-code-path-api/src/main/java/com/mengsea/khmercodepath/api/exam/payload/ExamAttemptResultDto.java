package com.mengsea.khmercodepath.api.exam.payload;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class ExamAttemptResultDto {
    Long examId;
    Integer score;
    int totalQuestions;
    BigDecimal scorePercent;
    String status;
    String failReason;
    LocalDateTime submittedAt;
}
