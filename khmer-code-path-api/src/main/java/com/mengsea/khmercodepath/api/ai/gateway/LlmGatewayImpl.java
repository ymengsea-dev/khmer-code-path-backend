package com.mengsea.khmercodepath.api.ai.gateway;

import com.mengsea.khmercodepath.commons.constant.ChatMessageRole;
import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LlmGatewayImpl implements LlmGateway {

    private final ChatClient tutoringChatClient;
    private final VectorStore vectorStore;

    public LlmGatewayImpl(
            @Qualifier("tutoringChatClient") ChatClient tutoringChatClient,
            VectorStore vectorStore
    ) {
        this.tutoringChatClient = tutoringChatClient;
        this.vectorStore = vectorStore;
    }

    @Override
    public String completeWithHistory(List<AiChatMessage> history, String userMessage) {
        List<Message> messages = new ArrayList<>();
        for (AiChatMessage prior : history) {
            if (prior.getRole() == ChatMessageRole.USER) {
                messages.add(new UserMessage(prior.getContent()));
            } else if (prior.getRole() == ChatMessageRole.ASSISTANT) {
                messages.add(new AssistantMessage(prior.getContent()));
            }
        }
        messages.add(new UserMessage(userMessage));

        return tutoringChatClient
                .prompt()
                .messages(messages)
                .call()
                .content();
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
        return tutoringChatClient.prompt()
                .user(question)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build())
                .call()
                .content();
    }
}
