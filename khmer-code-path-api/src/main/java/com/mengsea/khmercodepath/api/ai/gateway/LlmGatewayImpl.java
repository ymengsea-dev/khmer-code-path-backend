package com.mengsea.khmercodepath.api.ai.gateway;

import com.mengsea.khmercodepath.commons.constant.ChatMessageRole;
import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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
        return tutoringChatClient
                .prompt()
                .messages(buildMessages(history, userMessage))
                .call()
                .content();
    }

    @Override
    public String completeWithHistory(String systemPrompt, List<AiChatMessage> history, String userMessage) {
        return tutoringChatClient
                .prompt()
                .messages(buildMessages(systemPrompt, history, userMessage))
                .call()
                .content();
    }

    @Override
    public Flux<String> streamWithHistory(List<AiChatMessage> history, String userMessage) {
        return tutoringChatClient
                .prompt()
                .messages(buildMessages(history, userMessage))
                .stream()
                .content();
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
            if (prior.getRole() == ChatMessageRole.USER) {
                messages.add(new UserMessage(prior.getContent()));
            } else if (prior.getRole() == ChatMessageRole.ASSISTANT) {
                messages.add(new AssistantMessage(prior.getContent()));
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
        return tutoringChatClient.prompt()
                .user(question)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build())
                .call()
                .content();
    }

    @Override
    public String completeWithContent(String systemPrompt, String userContent) {
        return tutoringChatClient.prompt()
                .system(systemPrompt)
                .user(userContent)
                .call()
                .content();
    }
}
