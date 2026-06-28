package com.mengsea.khmercodepath.api.assignment.payload;

import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockDto;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class AssignmentDto {
    Long id;
    String title;
    String description;
    String instructions;
    Long classId;
    String className;
    String status;
    LocalDateTime createdAt;
    LocalDateTime dueAt;
    boolean pastDue;
    String submissionStatus;
    String submittedContent;
    Long enrolledStudents;
    Long submittedCount;
    java.math.BigDecimal submissionScorePercent;
    List<TaskContentBlockDto> contentBlocks;
}
