package com.mengsea.khmercodepath.api.ai.gateway;

import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Low-level adapter around Spring AI {@link org.springframework.ai.chat.client.ChatClient}.
 */
public interface LlmGateway {

    String completeWithHistory(List<AiChatMessage> history, String userMessage);

    String completeWithHistory(String systemPrompt, List<AiChatMessage> history, String userMessage);

    Flux<String> streamWithHistory(List<AiChatMessage> history, String userMessage);

    String completeRagQuery(String question, int topK);

    String completeRagQuery(String question);

    String completeRagQuery(String question, SearchRequest searchRequest);

    /**
     * Direct LLM call with explicit context — no vector store / RAG.
     * Use when the content is already available as plain text (e.g. template notes).
     *
     * @param systemPrompt instruction / task for the model
     * @param userContent  the content the model should reason over
     */
    String completeWithContent(String systemPrompt, String userContent);
}
