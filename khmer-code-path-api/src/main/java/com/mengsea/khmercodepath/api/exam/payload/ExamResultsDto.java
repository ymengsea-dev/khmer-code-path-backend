package com.mengsea.khmercodepath.api.exam.payload;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ExamResultsDto {
    ExamDto exam;
    long enrolledStudents;
    long submittedCount;
    long failedCount;
    long notStartedCount;
    Double averageScorePercent;
    Integer highestScore;
    Integer lowestScore;
    List<ExamSubmissionReviewDto> submissions;
}
