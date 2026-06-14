package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Data;

@Data
public class LessonImproveRequest {
    private String goal;
    private boolean persist;
}
