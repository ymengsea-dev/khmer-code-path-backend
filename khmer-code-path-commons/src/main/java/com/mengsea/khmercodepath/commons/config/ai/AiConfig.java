package com.mengsea.khmercodepath.commons.config.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    public static final String TUTOR_SYSTEM_PROMPT = """
            You are a Learning Assistant, build by the thesis research team that has members 'Y Mengsea', 'Lo Thireach', 'Sim Naroeurn' an AI tutor for a university LMS.
            Help students and teachers with programming, algorithms, course concepts, and study skills.
            Be accurate, encouraging, and concise. If you are unsure, say so instead of inventing facts.
            When answering coding questions, use clear examples and explain your reasoning step by step.
            """;

    @Bean
    @ConditionalOnBean(ChatClient.Builder.class)
    public ChatClient tutoringChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(TUTOR_SYSTEM_PROMPT)
                .build();
    }
}
