package com.mengsea.khmercodepath.api.progress.payload;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuizHistoryPayload {
    private Long submissionId;
    private Long quizId;
    private String quizTitle;
    private Long classId;
    private String className;
    private String status;
    private LocalDateTime submittedAt;
}
