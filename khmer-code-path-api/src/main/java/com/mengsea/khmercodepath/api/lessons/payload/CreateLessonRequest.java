package com.mengsea.khmercodepath.api.lessons.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLessonRequest {

    @NotNull
    private Long classId;

    @NotBlank
    @Size(max = 500)
    private String title;

    @Size(max = 10000)
    private String description;

    @Size(max = 128)
    private String moduleTag;

    /** Copy content from teacher library template. */
    private Long libraryItemId;
}
