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

    /** Max prior messages loaded from DB when calling the model (pairs of user/assistant). */
    private int historyWindow = 20;

    /** Run classpath document ingest on application startup (dev only). */
    private boolean ingestOnStartup = false;
}
