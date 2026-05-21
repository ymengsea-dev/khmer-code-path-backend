package com.mengsea.khmercodepath.api.notifications.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengsea.khmercodepath.api.notifications.payload.NotificationPayload;
import com.mengsea.khmercodepath.api.notifications.payload.UnreadCountPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSseHub {

    /**
     * Long-lived SSE must not hold a DB connection for the whole request (see open-in-view=false).
     * Clients reconnect before this expires; avoids infinite connections with timeout=0.
     */
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final ObjectMapper objectMapper;
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    /**
     * Registers one active stream per user; closes any previous stream so reconnect loops
     * do not leak connections.
     */
    public SseEmitter subscribe(String userUuid) {
        closeAllForUser(userUuid);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emittersByUser.computeIfAbsent(userUuid, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userUuid, emitter));
        emitter.onTimeout(() -> removeEmitter(userUuid, emitter));
        emitter.onError(ex -> removeEmitter(userUuid, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("{\"status\":\"ok\"}"));
        } catch (IOException ex) {
            removeEmitter(userUuid, emitter);
        }
        return emitter;
    }

    public void publishNotification(String userUuid, NotificationPayload payload, long unreadCount) {
        sendEvent(userUuid, "notification", payload);
        sendEvent(userUuid, "unread_count", UnreadCountPayload.builder().count(unreadCount).build());
    }

    public void publishUnreadCount(String userUuid, long unreadCount) {
        sendEvent(userUuid, "unread_count", UnreadCountPayload.builder().count(unreadCount).build());
    }

    private void sendEvent(String userUuid, String eventName, Object data) {
        List<SseEmitter> emitters = emittersByUser.get(userUuid);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize SSE payload for user {}", userUuid, ex);
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(json));
            } catch (IOException ex) {
                removeEmitter(userUuid, emitter);
            }
        }
    }

    public void sendHeartbeat() {
        emittersByUser.forEach((userUuid, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException ex) {
                    removeEmitter(userUuid, emitter);
                }
            }
        });
    }

    private void closeAllForUser(String userUuid) {
        List<SseEmitter> existing = emittersByUser.remove(userUuid);
        if (existing == null) {
            return;
        }
        for (SseEmitter emitter : existing) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // already closed
            }
        }
    }

    private void removeEmitter(String userUuid, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userUuid);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userUuid);
        }
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // already closed
        }
    }
}
