package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.payload.ChatMessagePayload;
import com.mengsea.khmercodepath.api.ai.payload.ChatReplyPayload;
import com.mengsea.khmercodepath.api.ai.payload.ConversationPayload;
import com.mengsea.khmercodepath.api.ai.payload.CreateConversationRequest;
import com.mengsea.khmercodepath.commons.constant.AiSectionType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiConversationService {

    ConversationPayload createConversation(CreateConversationRequest request);

    List<ConversationPayload> listConversations(AiSectionType sectionType, String sectionRef);

    List<ChatMessagePayload> listMessages(String conversationId);

    ChatReplyPayload sendMessage(String conversationId, String content);

    SseEmitter streamMessage(String conversationId, String content);

    void clearMessages(String conversationId);

    void deleteConversation(String conversationId);

    ConversationPayload renameConversation(String conversationId, String title);
}
