package com.mengsea.khmercodepath.api.ai.gateway;

import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;

/**
 * Low-level adapter around Spring AI {@link org.springframework.ai.chat.client.ChatClient}.
 */
public interface LlmGateway {

    String completeWithHistory(List<AiChatMessage> history, String userMessage);

    String completeRagQuery(String question, int topK);

    String completeRagQuery(String question);

    String completeRagQuery(String question, SearchRequest searchRequest);
}
