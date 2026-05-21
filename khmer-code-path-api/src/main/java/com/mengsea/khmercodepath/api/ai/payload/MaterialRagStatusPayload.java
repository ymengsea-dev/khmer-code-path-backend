package com.mengsea.khmercodepath.api.ai.payload;

import com.mengsea.khmercodepath.commons.constant.RagIndexStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class MaterialRagStatusPayload {
    Long materialId;
    Long lessonId;
    RagIndexStatus status;
    int chunkCount;
    LocalDateTime indexedAt;
    String errorMessage;
}
