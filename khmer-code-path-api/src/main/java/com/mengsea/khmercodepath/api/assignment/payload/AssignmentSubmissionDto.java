package com.mengsea.khmercodepath.api.assignment.payload;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AssignmentSubmissionDto {
    Long submissionId;
    String studentId;
    String studentName;
    String studentEmail;
    String status;
    String content;
    String feedback;
    String grade;
    LocalDateTime submittedAt;
}
