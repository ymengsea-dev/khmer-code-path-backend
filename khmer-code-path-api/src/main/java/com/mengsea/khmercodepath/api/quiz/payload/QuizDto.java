package com.mengsea.khmercodepath.api.quiz.payload;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class QuizDto {
    Long id;
    String title;
    String description;
    Long classId;
    String className;
    int questionCount;
    Integer durationMinutes;
    String status;
    LocalDateTime createdAt;
    LocalDateTime dueAt;
    /** ASSIGNMENT or EXAM */
    String kind;
    /** True when kind is EXAM — client enables proctoring rules. */
    boolean strictProctoring;
    /** True when dueAt is in the past (student UX). */
    boolean pastDue;
    /** Questions included only when fetching a single quiz for taking/preview. */
    List<QuizQuestionDto> questions;
    /** Student's own submission status — null if not yet submitted. */
    String submissionStatus;
    /** Raw generated JSON — included only when a teacher fetches a single quiz (for republishing). */
    String generatedContent;
    /** Teacher list/review counters. */
    Long enrolledStudents;
    Long submittedCount;
    Long failedCount;
}
