package com.mengsea.khmercodepath.api.exam.payload;

import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockInput;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateExamRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long classId;

    private LocalDateTime dueAt;

    private Integer durationMinutes;

    private String generatedContent;

    @Min(0)
    private int questionCount;

    private List<TaskContentBlockInput> contentBlocks;
}
