package com.mengsea.khmercodepath.api.lessons.payload;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonSummaryPayload {
    private Long id;
    private Long classId;
    private String className;
    private String title;
    private String moduleTag;
    private long materialCount;
    private boolean aiReady;
    private LocalDateTime createdAt;
}
