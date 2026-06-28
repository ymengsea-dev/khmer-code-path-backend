package com.mengsea.khmercodepath.api.assignmentsexams.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskContentBlockDto {
    String kind;
    int orderIndex;
    String label;

    String generatedContent;
    Integer questionCount;
    String sourceLabel;

    String storageKey;
    String fileName;
    String contentType;
    Long sizeBytes;
    String downloadUrl;

    String sourceKind;
    Long lessonId;
    Long libraryItemId;
    Long materialId;
    String htmlContent;
}
