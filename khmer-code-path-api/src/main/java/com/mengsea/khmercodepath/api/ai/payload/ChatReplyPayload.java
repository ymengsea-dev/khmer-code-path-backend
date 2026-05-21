package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ChatReplyPayload {
    String conversationId;
    ChatMessagePayload userMessage;
    ChatMessagePayload assistantMessage;
    List<ChatMessagePayload> messages;
}
