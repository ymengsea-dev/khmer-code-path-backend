package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.gateway.LlmGateway;
import com.mengsea.khmercodepath.api.ai.payload.ChatMessagePayload;
import com.mengsea.khmercodepath.api.ai.payload.ChatReplyPayload;
import com.mengsea.khmercodepath.api.ai.payload.ConversationPayload;
import com.mengsea.khmercodepath.api.ai.payload.CreateConversationRequest;
import com.mengsea.khmercodepath.commons.constant.AiSectionType;
import com.mengsea.khmercodepath.commons.constant.ChatMessageRole;
import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import com.mengsea.khmercodepath.commons.domain.AiConversation;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.repository.AiChatMessageRepository;
import com.mengsea.khmercodepath.commons.repository.AiConversationRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationRepository conversationRepository;
    private final AiChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final LlmGateway llmGateway;
    private final AiConversationTxHelper txHelper;

    @Override
    @Transactional
    public ConversationPayload createConversation(CreateConversationRequest request) {
        String userUuid = SecurityUtils.requireCurrentUser().getUuid();
        User user = userRepository.getReferenceById(userUuid);

        AiConversation entity = new AiConversation();
        entity.setId(UUID.randomUUID().toString());
        entity.setUser(user);
        entity.setSectionType(request.getSectionType() != null ? request.getSectionType() : AiSectionType.GENERAL);
        entity.setSectionRef(blankToNull(request.getSectionRef()));
        entity.setTitle(resolveTitle(request.getTitle()));
        entity.setDeleted(false);
        conversationRepository.save(entity);
        return toConversationPayload(entity, "");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationPayload> listConversations(AiSectionType sectionType, String sectionRef) {
        String userUuid = SecurityUtils.requireCurrentUser().getUuid();
        List<AiConversation> rows;
        if (sectionType != null && sectionRef != null && !sectionRef.isBlank()) {
            rows = conversationRepository.findByUser_UuidAndSectionTypeAndSectionRefAndDeletedFalseOrderByUpdatedAtDesc(
                    userUuid, sectionType, sectionRef.trim());
        } else {
            rows = conversationRepository.findByUser_UuidAndDeletedFalseOrderByUpdatedAtDesc(userUuid);
        }
        return rows.stream()
                .map(c -> toConversationPayload(c, previewFor(c.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessagePayload> listMessages(String conversationId) {
        txHelper.requireOwnedConversation(conversationId);
        return messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId).stream()
                .map(m -> toMessagePayload(m, conversationId))
                .toList();
    }

    @Override
    public ChatReplyPayload sendMessage(String conversationId, String content) {
        String trimmed = content.trim();
        txHelper.requireOwnedConversation(conversationId);

        List<AiChatMessage> history = txHelper.loadHistoryWindow(conversationId);

        // Do not hold a JDBC connection while waiting on the external LLM API.
        String assistantText = llmGateway.completeWithHistory(history, trimmed);
        if (assistantText == null || assistantText.isBlank()) {
            assistantText = "I could not generate a response. Please try again.";
        }

        return txHelper.persistExchange(conversationId, trimmed, assistantText);
    }

    @Override
    @Transactional
    public void clearMessages(String conversationId) {
        txHelper.requireOwnedConversation(conversationId);
        messageRepository.deleteByConversation_Id(conversationId);
    }

    @Override
    @Transactional
    public void deleteConversation(String conversationId) {
        AiConversation conversation = txHelper.requireOwnedConversation(conversationId);
        conversation.setDeleted(true);
        conversationRepository.save(conversation);
    }

    private String previewFor(String conversationId) {
        return messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId).stream()
                .filter(m -> m.getRole() == ChatMessageRole.USER)
                .reduce((first, second) -> second)
                .map(m -> truncatePreview(m.getContent()))
                .orElse("No messages yet");
    }

    private ConversationPayload toConversationPayload(AiConversation entity, String preview) {
        long count = messageRepository.countByConversation_Id(entity.getId());
        return ConversationPayload.builder()
                .id(entity.getId())
                .sectionType(entity.getSectionType())
                .sectionRef(entity.getSectionRef())
                .title(entity.getTitle())
                .preview(preview)
                .messageCount(count)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ChatMessagePayload toMessagePayload(AiChatMessage entity, String conversationId) {
        return ChatMessagePayload.builder()
                .id(entity.getId())
                .conversationId(conversationId)
                .role(entity.getRole())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static String resolveTitle(String title) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        return "New conversation";
    }

    private static String truncatePreview(String text) {
        String t = text.replace('\n', ' ').trim();
        return t.length() > 80 ? t.substring(0, 77) + "..." : t;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
