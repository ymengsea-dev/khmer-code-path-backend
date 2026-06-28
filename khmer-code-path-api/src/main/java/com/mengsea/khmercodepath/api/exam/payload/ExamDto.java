package com.mengsea.khmercodepath.api.exam.payload;

import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockDto;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class ExamDto {
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
    boolean pastDue;
    List<ExamQuestionDto> questions;
    String submissionStatus;
    String generatedContent;
    Long enrolledStudents;
    Long submittedCount;
    Long failedCount;
    List<TaskContentBlockDto> contentBlocks;
}
