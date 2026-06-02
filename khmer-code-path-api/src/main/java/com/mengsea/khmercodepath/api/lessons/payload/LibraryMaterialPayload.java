package com.mengsea.khmercodepath.api.lessons.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryMaterialPayload {
    private Long id;
    private Long libraryItemId;
    private String fileName;
    private String contentType;
    private long fileSizeBytes;
    private String ragStatus;
    /** True when the file lives in the teacher file pool (not on a lesson template). */
    private boolean poolFile;
}
