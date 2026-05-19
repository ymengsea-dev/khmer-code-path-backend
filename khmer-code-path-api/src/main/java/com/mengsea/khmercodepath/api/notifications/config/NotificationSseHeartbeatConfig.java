package com.mengsea.khmercodepath.api.notifications.config;

import com.mengsea.khmercodepath.api.notifications.sse.NotificationSseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Keeps SSE connections alive through proxies and load balancers.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class NotificationSseHeartbeatConfig {

    private final NotificationSseHub notificationSseHub;

    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        notificationSseHub.sendHeartbeat();
    }
}
