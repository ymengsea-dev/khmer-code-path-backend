package com.mengsea.khmercodepath.api.ai.payload;

import com.mengsea.khmercodepath.commons.constant.ChatMessageRole;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ChatMessagePayload {
    Long id;
    String conversationId;
    ChatMessageRole role;
    String content;
    LocalDateTime createdAt;
}
