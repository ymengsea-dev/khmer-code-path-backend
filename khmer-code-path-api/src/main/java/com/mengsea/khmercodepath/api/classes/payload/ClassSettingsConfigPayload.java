package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassSettingsConfigPayload {
    private Long classId;
    private String className;
    private List<LessonTabPayload> tabs;
    private List<ScoreComponentPayload> scoreComponents;
    private List<ClassStatusOptionPayload> statusOptions;
}
