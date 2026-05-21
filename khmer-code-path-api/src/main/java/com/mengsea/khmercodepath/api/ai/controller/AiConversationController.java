package com.mengsea.khmercodepath.api.ai.controller;

import com.mengsea.khmercodepath.api.ai.payload.ChatMessagePayload;
import com.mengsea.khmercodepath.api.ai.payload.ChatReplyPayload;
import com.mengsea.khmercodepath.api.ai.payload.ConversationPayload;
import com.mengsea.khmercodepath.api.ai.payload.CreateConversationRequest;
import com.mengsea.khmercodepath.api.ai.payload.SendChatMessageRequest;
import com.mengsea.khmercodepath.api.ai.service.AiConversationService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.AiSectionType;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/conversations")
@RequiredArgsConstructor
@Tag(name = "AI Conversations", description = "QNA — persistent tutoring chat per section/thread")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.AI_CHAT + "')")
public class AiConversationController {

    private final AiConversationService aiConversationService;

    @Operation(summary = "QNA-0700 · Create conversation")
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationPayload>> create(
            @Valid @RequestBody CreateConversationRequest request
    ) {
        ConversationPayload data = aiConversationService.createConversation(request);
        return ResponseEntity.ok(ApiResponses.of("QNA-0700", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QNA-0701 · List conversations")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationPayload>>> list(
            @RequestParam(required = false) AiSectionType sectionType,
            @RequestParam(required = false) String sectionRef
    ) {
        List<ConversationPayload> data = aiConversationService.listConversations(sectionType, sectionRef);
        return ResponseEntity.ok(ApiResponses.of("QNA-0701", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QNA-0702 · List messages in conversation")
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessagePayload>>> listMessages(
            @PathVariable String conversationId
    ) {
        List<ChatMessagePayload> data = aiConversationService.listMessages(conversationId);
        return ResponseEntity.ok(ApiResponses.of("QNA-0702", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QNA-0703 · Send message (persists user + assistant)")
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatReplyPayload>> sendMessage(
            @PathVariable String conversationId,
            @Valid @RequestBody SendChatMessageRequest request
    ) {
        ChatReplyPayload data = aiConversationService.sendMessage(conversationId, request.getContent());
        return ResponseEntity.ok(ApiResponses.of("QNA-0703", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QNA-0704 · Clear messages in conversation")
    @DeleteMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Void>> clearMessages(@PathVariable String conversationId) {
        aiConversationService.clearMessages(conversationId);
        return ResponseEntity.ok(ApiResponses.of("QNA-0704", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "QNA-0705 · Delete conversation (soft)")
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable String conversationId) {
        aiConversationService.deleteConversation(conversationId);
        return ResponseEntity.ok(ApiResponses.of("QNA-0705", LmsStatusCode.SUCCESS, null, null));
    }
}
