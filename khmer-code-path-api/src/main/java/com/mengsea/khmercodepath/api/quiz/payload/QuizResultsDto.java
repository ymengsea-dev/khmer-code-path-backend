package com.mengsea.khmercodepath.api.quiz.payload;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class QuizResultsDto {
    QuizDto quiz;
    long enrolledStudents;
    long submittedCount;
    long failedCount;
    long notStartedCount;
    Double averageScorePercent;
    Integer highestScore;
    Integer lowestScore;
    List<QuizSubmissionReviewDto> submissions;
}
