package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.config.LlmProperties;
import com.mengsea.khmercodepath.api.ai.payload.ChatMessagePayload;
import com.mengsea.khmercodepath.api.ai.payload.ChatReplyPayload;
import com.mengsea.khmercodepath.commons.constant.ChatMessageRole;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import com.mengsea.khmercodepath.commons.domain.AiConversation;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.AiChatMessageRepository;
import com.mengsea.khmercodepath.commons.repository.AiConversationRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
class AiConversationTxHelper {

    private final AiConversationRepository conversationRepository;
    private final AiChatMessageRepository messageRepository;
    private final LlmProperties llmProperties;

    public AiConversation requireOwnedConversation(String conversationId) {
        String userUuid = SecurityUtils.requireCurrentUser().getUuid();
        return conversationRepository.findByIdAndUser_UuidAndDeletedFalse(conversationId, userUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.AI_CONVERSATION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<AiChatMessage> loadHistoryWindow(String conversationId) {
        List<AiChatMessage> all = messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId);
        int window = Math.max(llmProperties.getHistoryWindow(), 2);
        if (all.size() <= window) {
            return all;
        }
        return all.subList(all.size() - window, all.size());
    }

    @Transactional
    public ChatReplyPayload persistExchange(String conversationId, String userContent, String assistantText) {
        AiConversation conversation = requireOwnedConversation(conversationId);

        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setConversation(conversation);
        userMessage.setRole(ChatMessageRole.USER);
        userMessage.setContent(userContent);
        messageRepository.save(userMessage);

        AiChatMessage assistantMessage = new AiChatMessage();
        assistantMessage.setConversation(conversation);
        assistantMessage.setRole(ChatMessageRole.ASSISTANT);
        assistantMessage.setContent(assistantText);
        messageRepository.save(assistantMessage);

        if (conversation.getTitle() == null || conversation.getTitle().isBlank()
                || "New conversation".equals(conversation.getTitle())) {
            conversation.setTitle(truncateTitle(userContent));
        }
        conversationRepository.save(conversation);

        List<ChatMessagePayload> allMessages = messageRepository
                .findByConversation_IdOrderByCreatedAtAsc(conversationId).stream()
                .map(m -> toMessagePayload(m, conversationId))
                .toList();

        return ChatReplyPayload.builder()
                .conversationId(conversationId)
                .userMessage(toMessagePayload(userMessage, conversationId))
                .assistantMessage(toMessagePayload(assistantMessage, conversationId))
                .messages(allMessages)
                .build();
    }

    private static ChatMessagePayload toMessagePayload(AiChatMessage entity, String conversationId) {
        return ChatMessagePayload.builder()
                .id(entity.getId())
                .conversationId(conversationId)
                .role(entity.getRole())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static String truncateTitle(String text) {
        String t = text.replace('\n', ' ').trim();
        return t.length() > 60 ? t.substring(0, 57) + "..." : t;
    }
}
