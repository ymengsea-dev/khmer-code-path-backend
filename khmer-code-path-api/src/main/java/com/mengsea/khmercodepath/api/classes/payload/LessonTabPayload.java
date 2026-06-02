package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonTabPayload {
    private String id;
    private String label;
}
