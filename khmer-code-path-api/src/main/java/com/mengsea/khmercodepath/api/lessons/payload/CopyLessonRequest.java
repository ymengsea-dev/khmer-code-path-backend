package com.mengsea.khmercodepath.api.lessons.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CopyLessonRequest {

    @NotNull
    private Long targetClassId;

    private boolean includeMaterials = true;
}
