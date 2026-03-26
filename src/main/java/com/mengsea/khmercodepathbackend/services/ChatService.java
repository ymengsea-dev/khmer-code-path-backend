package com.mengsea.khmercodepathbackend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;

    // blocking return full response at once
    public String chat(String message){
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    // streaming return token as string
    public Flux<String> stream(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    // With a system instruction override
    public String chatAs(String role, String message) {
        return chatClient.prompt()
                .system("You are " + role + ". Stay in character.")
                .user(message)
                .call()
                .content();
    }
}
