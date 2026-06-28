package com.mengsea.khmercodepath.api.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "lms.ai")
public class LlmProperties {

    /** When false, Spring AI autoconfig is skipped and AI endpoints return 503. */
    private boolean enabled = true;

    /** Max prior messages loaded from DB when calling the model (pairs of user/assistant). */
    private int historyWindow = 20;

    /** Run classpath document ingest on application startup (dev only). */
    private boolean ingestOnStartup = false;

    /** How long a successful AI health probe stays cached (seconds). */
    private int healthCheckCacheSeconds = 30;

    /** How long a failed AI health probe stays cached (seconds). */
    private int healthCheckFailureCacheSeconds = 15;
}
