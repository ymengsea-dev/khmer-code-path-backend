package com.mengsea.khmercodepath.api.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Probes the configured LLM provider (Ollama via {@code OLLAMA_BASE_URL}) without blocking app startup.
 * Results are cached briefly so non-AI endpoints are not penalized on every request.
 */
@Service
@ConditionalOnProperty(name = "lms.ai.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class AiAvailabilityService {

    private final LlmProperties llmProperties;
    private final RestClient ollamaClient;
    private final AtomicReference<CachedProbe> cachedProbe = new AtomicReference<>(CachedProbe.unknown());

    public AiAvailabilityService(
            LlmProperties llmProperties,
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl
    ) {
        this.llmProperties = llmProperties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2_000);
        factory.setReadTimeout(3_000);
        String normalizedBase = ollamaBaseUrl.endsWith("/")
                ? ollamaBaseUrl.substring(0, ollamaBaseUrl.length() - 1)
                : ollamaBaseUrl;
        this.ollamaClient = RestClient.builder()
                .baseUrl(normalizedBase)
                .requestFactory(factory)
                .build();
    }

    public boolean isEnabled() {
        return llmProperties.isEnabled();
    }

    public boolean isAvailable() {
        if (!llmProperties.isEnabled()) {
            return false;
        }
        CachedProbe cached = cachedProbe.get();
        if (cached.isFresh()) {
            return cached.available();
        }
        return probeAndCache();
    }

    public void markUnavailable(Throwable cause) {
        log.debug("Marking AI provider unavailable after call failure: {}", cause.getMessage());
        cachedProbe.set(CachedProbe.unavailable(
                Instant.now().plusSeconds(llmProperties.getHealthCheckFailureCacheSeconds())
        ));
    }

    public String providerLabel() {
        return "ollama";
    }

    private boolean probeAndCache() {
        boolean available = probeOllama();
        Instant expiresAt = Instant.now().plusSeconds(
                available
                        ? llmProperties.getHealthCheckCacheSeconds()
                        : llmProperties.getHealthCheckFailureCacheSeconds()
        );
        cachedProbe.set(new CachedProbe(available, expiresAt));
        return available;
    }

    private boolean probeOllama() {
        try {
            ollamaClient.get().uri("/api/tags").retrieve().toBodilessEntity();
            return true;
        } catch (Exception ex) {
            log.debug("Ollama health probe failed: {}", ex.getMessage());
            return false;
        }
    }

    private record CachedProbe(boolean available, Instant expiresAt) {
        static CachedProbe unknown() {
            return new CachedProbe(false, Instant.EPOCH);
        }

        static CachedProbe unavailable(Instant expiresAt) {
            return new CachedProbe(false, expiresAt);
        }

        boolean isFresh() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
