package com.mengsea.khmercodepath.api.ai.gateway;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@ConditionalOnProperty(name = "lms.ai.enabled", havingValue = "false")
public class DisabledLlmGateway implements LlmGateway {

    @Override
    public String completeWithHistory(List<AiChatMessage> history, String userMessage) {
        throw unavailable();
    }

    @Override
    public String completeWithHistory(String systemPrompt, List<AiChatMessage> history, String userMessage) {
        throw unavailable();
    }

    @Override
    public Flux<String> streamWithHistory(List<AiChatMessage> history, String userMessage) {
        return Flux.error(unavailable());
    }

    @Override
    public String completeRagQuery(String question) {
        throw unavailable();
    }

    @Override
    public String completeRagQuery(String question, int topK) {
        throw unavailable();
    }

    @Override
    public String completeRagQuery(String question, SearchRequest searchRequest) {
        throw unavailable();
    }

    @Override
    public String completeWithContent(String systemPrompt, String userContent) {
        throw unavailable();
    }

    private static BusinessException unavailable() {
        return new BusinessException(ExceptionCode.AI_SERVICE_UNAVAILABLE);
    }
}
