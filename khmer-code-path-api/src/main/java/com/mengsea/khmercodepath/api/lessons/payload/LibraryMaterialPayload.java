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
}
