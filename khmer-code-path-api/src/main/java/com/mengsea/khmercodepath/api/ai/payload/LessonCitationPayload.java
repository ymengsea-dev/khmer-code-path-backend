package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonCitationPayload {
    private String sourceType;
    private Long materialId;
    private String sourceName;
    private Integer chunkIndex;
    private String excerpt;
}
