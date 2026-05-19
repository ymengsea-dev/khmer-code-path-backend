package com.mengsea.khmercodepath.api.lessons.payload;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLessonRequest {

    @Size(max = 500)
    private String title;

    @Size(max = 10000)
    private String description;

    @Size(max = 10000)
    private String summary;

    @Size(max = 128)
    private String moduleTag;
}
