package com.mengsea.khmercodepathbackend.controller;

import com.google.genai.errors.ClientException;
import com.mengsea.khmercodepathbackend.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class LLMController {
    private final ChatService chatService;

    // GET /api/chat?message=Hello
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatService.chat(message);
    }

    // GET /api/chat/stream?message=Tell me a story
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return chatService.stream(message);
    }

}
