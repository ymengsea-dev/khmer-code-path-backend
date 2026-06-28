package com.mengsea.khmercodepath.api.assignment.payload;

import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockInput;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateAssignmentRequest {

    @NotBlank
    private String title;

    private String description;

    private String instructions;

    @NotNull
    private Long classId;

    private LocalDateTime dueAt;

    private List<TaskContentBlockInput> contentBlocks;
}
