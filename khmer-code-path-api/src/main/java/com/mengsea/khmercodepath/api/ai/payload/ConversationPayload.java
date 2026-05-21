package com.mengsea.khmercodepath.api.ai.payload;

import com.mengsea.khmercodepath.commons.constant.AiSectionType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ConversationPayload {
    String id;
    AiSectionType sectionType;
    String sectionRef;
    String title;
    String preview;
    long messageCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
