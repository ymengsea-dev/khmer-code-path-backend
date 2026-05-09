package com.mengsea.khmercodepathbackend.controller;

import com.mengsea.khmercodepathbackend.constant.LmsStatusCode;
import com.mengsea.khmercodepathbackend.dto.advices.ApiResponse;
import com.mengsea.khmercodepathbackend.dto.advices.ApiResponses;
import com.mengsea.khmercodepathbackend.services.ai.ChatService;
import com.mengsea.khmercodepathbackend.services.ai.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class LLMController {

    private final ChatService chatService;
    private final RAGService ragService;

    // GET /api/chat?message=Hello
    @GetMapping("/chat")
    public ApiResponse<String> chat(@RequestParam String message) {
        return ApiResponses.of("QNA-0700", LmsStatusCode.SUCCESS, null, chatService.chat(message));
    }

    // GET /api/chat/stream?message=Tell me a story
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return chatService.stream(message);
    }

    // RAG -- call once to load documents
    @PostMapping("/rag/ingest")
    public ApiResponse<String> ingest() throws Exception {
        ragService.ingestDocument();;
        return ApiResponses.of("LSN-0450", LmsStatusCode.SUCCESS, null, "Document ingest successfully.");
    }

    // uery?question=What is ... ?
    @GetMapping("/rag/query")
    public ApiResponse<String> query(@RequestParam String question){
        return ApiResponses.of("QNA-0710", LmsStatusCode.SUCCESS, null, ragService.query(question));
    }

    @GetMapping("/rag/query/advanced")
    public ApiResponse<String> queryAdvanced(
            @RequestParam String question,
            @RequestParam(defaultValue = "4") int topK
    ) {
        return ApiResponses.of("QNA-0710", LmsStatusCode.SUCCESS, null, ragService.queryWithOptions(question, topK));
    }

}
