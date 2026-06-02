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
import com.mengsea.khmercodepath.commons.domain.Lesson;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.repository.AiChatMessageRepository;
import com.mengsea.khmercodepath.commons.repository.AiConversationRepository;
import com.mengsea.khmercodepath.commons.repository.LessonRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationRepository conversationRepository;
    private final AiChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ClassAccessHelper classAccessHelper;
    private final LlmGateway llmGateway;
    private final AiConversationTxHelper txHelper;
    private final ObjectMapper objectMapper;

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
        AiConversation conversation = txHelper.requireOwnedConversation(conversationId);

        List<AiChatMessage> history = txHelper.loadHistoryWindow(conversationId);

        // Do not hold a JDBC connection while waiting on the external LLM API.
        String systemPrompt = lessonSystemPrompt(conversation);
        String assistantText = systemPrompt == null
                ? llmGateway.completeWithHistory(history, trimmed)
                : llmGateway.completeWithHistory(systemPrompt, history, trimmed);
        if (assistantText == null || assistantText.isBlank()) {
            assistantText = "I could not generate a response. Please try again.";
        }

        return txHelper.persistExchange(conversationId, trimmed, assistantText);
    }

    @Override
    public SseEmitter streamMessage(String conversationId, String content) {
        String trimmed = content.trim();

        // Validate ownership and persist user message on the request thread (security context available).
        List<AiChatMessage> history = txHelper.prepareStreamExchange(conversationId, trimmed);

        SseEmitter emitter = new SseEmitter(120_000L);

        // Use a virtual thread so emitter.send() runs on a stable thread with a valid async context.
        // Flux.toIterable() blocks the calling thread until each chunk is available — safe on virtual threads.
        Thread.ofVirtual().start(() -> {
            StringBuilder collected = new StringBuilder();
            try {
                for (String chunk : llmGateway.streamWithHistory(history, trimmed).toIterable()) {
                    if (chunk == null || chunk.isEmpty()) continue;
                    collected.append(chunk);
                    // JSON-encode the chunk so embedded newlines (\n) and leading spaces are
                    // preserved as escape sequences in the SSE data field. Without this,
                    // a chunk like " hello\nworld" would be split across two SSE lines and
                    // the leading space would be consumed by the SSE protocol.
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(chunk)));
                }
                txHelper.persistStreamedAssistantMessage(conversationId, collected.toString(), trimmed);
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (JsonProcessingException e) {
                emitter.completeWithError(e);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
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

    @Override
    @Transactional
    public ConversationPayload renameConversation(String conversationId, String title) {
        AiConversation conversation = txHelper.requireOwnedConversation(conversationId);
        conversation.setTitle(title.trim());
        conversationRepository.save(conversation);
        return toConversationPayload(conversation, previewFor(conversationId));
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

    private String lessonSystemPrompt(AiConversation conversation) {
        if (conversation.getSectionType() != AiSectionType.LESSON || conversation.getSectionRef() == null) {
            return null;
        }

        Long lessonId;
        try {
            lessonId = Long.valueOf(conversation.getSectionRef());
        } catch (NumberFormatException ex) {
            return null;
        }

        return lessonRepository.findByIdAndDeletedFalseWithClass(lessonId)
                .map(lesson -> {
                    classAccessHelper.assertCanRead(lesson.getLmsClass());
                    String content = stripHtml(lesson.getDescription());
                    if (content.isBlank()) {
                        return null;
                    }
                    return buildLessonPrompt(lesson, content);
                })
                .orElse(null);
    }

    private static String buildLessonPrompt(Lesson lesson, String lessonContent) {
        return """
                You are a helpful study assistant for one specific lesson.
                Answer the student using only the lesson context below and the chat history.
                If the answer is not present in the lesson context, say that the lesson does not cover it clearly.
                Keep explanations student-friendly and concise.

                Lesson title: %s

                Lesson context:
                %s
                """.formatted(lesson.getTitle(), lessonContent);
    }

    private static String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html
                .replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
