package com.mengsea.khmercodepath.api.ai.controller;

import com.mengsea.khmercodepath.api.ai.service.AiRagService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/rag")
@RequiredArgsConstructor
@Tag(name = "AI RAG", description = "Document-grounded answers from the vector store")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.AI_CHAT + "')")
public class AiRagController {

    private final AiRagService aiRagService;

    @Operation(summary = "LSN-0450 · (Deprecated) Classpath ingest — use per-material indexing via lesson AI endpoints")
    @PostMapping("/ingest")
    @PreAuthorize("hasAuthority('" + LmsAuthority.AI_INGEST + "')")
    public ResponseEntity<ApiResponse<String>> ingest() {
        aiRagService.ingestClasspathDocuments();
        return ResponseEntity.ok(
                ApiResponses.of("LSN-0450", LmsStatusCode.SUCCESS, null,
                        "Classpath ingest completed (deprecated). Materials are indexed on-demand from MinIO.")
        );
    }

    @Operation(summary = "QNA-0710 · RAG query")
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<String>> query(@RequestParam String question) {
        String answer = aiRagService.query(question);
        return ResponseEntity.ok(ApiResponses.of("QNA-0710", LmsStatusCode.SUCCESS, null, answer));
    }

    @Operation(summary = "QNA-0711 · RAG query with topK")
    @GetMapping("/query/advanced")
    public ResponseEntity<ApiResponse<String>> queryAdvanced(
            @RequestParam String question,
            @RequestParam(defaultValue = "4") int topK
    ) {
        String answer = aiRagService.query(question, topK);
        return ResponseEntity.ok(ApiResponses.of("QNA-0711", LmsStatusCode.SUCCESS, null, answer));
    }
}
