package com.mengsea.khmercodepath.api.llm.controller;

import com.mengsea.khmercodepath.api.llm.service.ChatService;
import com.mengsea.khmercodepath.api.llm.service.RAGService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class LLMController {

    private final ChatService chatService;
    private final RAGService ragService;

    @GetMapping("/chat")
    public ApiResponse<String> chat(@RequestParam String message) {
        return ApiResponses.of("QNA-0700", LmsStatusCode.SUCCESS, null, chatService.chat(message));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return chatService.stream(message);
    }

    @PostMapping("/rag/ingest")
    public ApiResponse<String> ingest() throws Exception {
        ragService.ingestDocument();
        return ApiResponses.of("LSN-0450", LmsStatusCode.SUCCESS, null, "Document ingest successfully.");
    }

    @GetMapping("/rag/query")
    public ApiResponse<String> query(@RequestParam String question) {
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
