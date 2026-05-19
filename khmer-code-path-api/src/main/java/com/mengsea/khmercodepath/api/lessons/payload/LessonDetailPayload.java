package com.mengsea.khmercodepath.api.lessons.payload;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LessonDetailPayload {
    private Long id;
    private Long classId;
    private String className;
    private String title;
    private String description;
    private String summary;
    private String moduleTag;
    private boolean aiReady;
    private boolean materialsProcessing;
    private List<LessonMaterialPayload> materials;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
