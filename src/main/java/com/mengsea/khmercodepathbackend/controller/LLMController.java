package com.mengsea.khmercodepathbackend.controller;

import com.mengsea.khmercodepathbackend.services.ai.ChatService;
import com.mengsea.khmercodepathbackend.services.ai.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class LLMController {

    private final ChatService chatService;
    private final RAGService ragService;

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

    // RAG -- call once to load documents
    @PostMapping("/rag/ingest")
    public String ingest() throws Exception {
        ragService.ingestDocument();;
        return "Document ingest successfully.";
    }

    // uery?question=What is ... ?
    @GetMapping("/rag/query")
    public String query(@RequestParam String question){
        return ragService.query(question);
    }

    @GetMapping("/rag/query/advanced")
    public String queryAdvanced(
            @RequestParam String question,
            @RequestParam(defaultValue = "4") int topK
    ) {
        return ragService.queryWithOptions(question, topK);
    }

}
