package com.mengsea.khmercodepath.api.assignmentsexams.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskContentBlockInput {

    /** AI_QUESTIONS | FILE | LIBRARY_SOURCE */
    private String kind;

    private Integer orderIndex;

    /** AI_QUESTIONS */
    private String generatedContent;
    private Integer questionCount;
    private String sourceLabel;

    /** FILE */
    private String storageKey;
    private String fileName;
    private String contentType;
    private Long sizeBytes;

    /** LIBRARY_SOURCE — sourceKind: lesson | library | library-content */
    private String sourceKind;
    private Long lessonId;
    private Long libraryItemId;
    private Long materialId;
    private String label;
}
