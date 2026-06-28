package com.mengsea.khmercodepath.api.ai.controller;

import com.mengsea.khmercodepath.api.ai.config.AiAvailabilityService;
import com.mengsea.khmercodepath.api.ai.config.LlmProperties;
import com.mengsea.khmercodepath.api.ai.payload.AiStatusPayload;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Status", description = "AI provider availability (for UI banners and ops)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class AiStatusController {

    private final LlmProperties llmProperties;
    private final ObjectProvider<AiAvailabilityService> aiAvailabilityProvider;

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Operation(summary = "QNA-0709 · AI provider status")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<AiStatusPayload>> status() {
        boolean enabled = llmProperties.isEnabled();
        AiAvailabilityService availability = aiAvailabilityProvider.getIfAvailable();
        boolean available = enabled && availability != null && availability.isAvailable();
        String provider = enabled && availability != null ? availability.providerLabel() : "disabled";

        AiStatusPayload payload = AiStatusPayload.builder()
                .enabled(enabled)
                .available(available)
                .provider(provider)
                .baseUrl(enabled ? ollamaBaseUrl : null)
                .build();

        return ResponseEntity.ok(ApiResponses.of("QNA-0709", LmsStatusCode.SUCCESS, null, payload));
    }
}
