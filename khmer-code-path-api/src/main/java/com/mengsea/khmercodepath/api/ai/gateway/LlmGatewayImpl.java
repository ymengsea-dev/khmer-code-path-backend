package com.mengsea.khmercodepath.api.ai.gateway;

import com.mengsea.khmercodepath.api.ai.config.AiAvailabilityService;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "lms.ai.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class LlmGatewayImpl implements LlmGateway {

    private final ChatClient tutoringChatClient;
    private final VectorStore vectorStore;
    private final AiAvailabilityService aiAvailability;

    public LlmGatewayImpl(
            @Qualifier("tutoringChatClient") ChatClient tutoringChatClient,
            VectorStore vectorStore,
            AiAvailabilityService aiAvailability
    ) {
        this.tutoringChatClient = tutoringChatClient;
        this.vectorStore = vectorStore;
        this.aiAvailability = aiAvailability;
    }

    @Override
    public String completeWithHistory(List<AiChatMessage> history, String userMessage) {
        return call(() -> tutoringChatClient
                .prompt()
                .messages(buildMessages(history, userMessage))
                .call()
                .content());
    }

    @Override
    public String completeWithHistory(String systemPrompt, List<AiChatMessage> history, String userMessage) {
        return call(() -> tutoringChatClient
                .prompt()
                .messages(buildMessages(systemPrompt, history, userMessage))
                .call()
                .content());
    }

    @Override
    public Flux<String> streamWithHistory(List<AiChatMessage> history, String userMessage) {
        ensureAvailable();
        return tutoringChatClient
                .prompt()
                .messages(buildMessages(history, userMessage))
                .stream()
                .content()
                .onErrorMap(this::mapFailure);
    }

    private List<Message> buildMessages(List<AiChatMessage> history, String userMessage) {
        List<Message> messages = new ArrayList<>();
        appendHistory(messages, history);
        messages.add(new UserMessage(userMessage));
        return messages;
    }

    private List<Message> buildMessages(String systemPrompt, List<AiChatMessage> history, String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        appendHistory(messages, history);
        messages.add(new UserMessage(userMessage));
        return messages;
    }

    private void appendHistory(List<Message> messages, List<AiChatMessage> history) {
        for (AiChatMessage prior : history) {
            switch (prior.getRole()) {
                case USER -> messages.add(new UserMessage(prior.getContent()));
                case ASSISTANT -> messages.add(new AssistantMessage(prior.getContent()));
                default -> {
                }
            }
        }
    }

    @Override
    public String completeRagQuery(String question) {
        return completeRagQuery(question, 4);
    }

    @Override
    public String completeRagQuery(String question, int topK) {
        return completeRagQuery(question, SearchRequest.builder()
                .query(question)
                .topK(topK)
                .build());
    }

    @Override
    public String completeRagQuery(String question, SearchRequest searchRequest) {
        return call(() -> tutoringChatClient.prompt()
                .user(question)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build())
                .call()
                .content());
    }

    @Override
    public String completeWithContent(String systemPrompt, String userContent) {
        return call(() -> tutoringChatClient.prompt()
                .system(systemPrompt)
                .user(userContent)
                .call()
                .content());
    }

    private String call(Supplier<String> action) {
        ensureAvailable();
        try {
            return action.get();
        } catch (RuntimeException ex) {
            throw mapFailure(ex);
        }
    }

    private void ensureAvailable() {
        if (!aiAvailability.isAvailable()) {
            throw new BusinessException(ExceptionCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private RuntimeException mapFailure(Throwable ex) {
        if (ex instanceof BusinessException businessException) {
            return businessException;
        }
        log.debug("AI provider call failed: {}", ex.getMessage());
        aiAvailability.markUnavailable(ex);
        return new BusinessException(ExceptionCode.AI_SERVICE_UNAVAILABLE);
    }
}
