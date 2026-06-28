package com.mengsea.khmercodepath.api.assignmentsexams.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskContentUploadResponse {
    String storageKey;
    String fileName;
    String contentType;
    long sizeBytes;
}
