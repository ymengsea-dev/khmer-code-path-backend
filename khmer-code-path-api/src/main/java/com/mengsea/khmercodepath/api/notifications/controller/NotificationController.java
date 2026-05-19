package com.mengsea.khmercodepath.api.notifications.controller;

import com.mengsea.khmercodepath.api.notifications.payload.NotificationListPayload;
import com.mengsea.khmercodepath.api.notifications.payload.NotificationPayload;
import com.mengsea.khmercodepath.api.notifications.payload.UnreadCountPayload;
import com.mengsea.khmercodepath.api.notifications.service.NotificationService;
import com.mengsea.khmercodepath.api.notifications.sse.NotificationSseHub;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "NOTIF — in-app notifications with SSE")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseHub notificationSseHub;

    @Operation(summary = "NOTIF-1400 · List notifications")
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListPayload>> list(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        NotificationListPayload data = notificationService.list(filter, limit, offset);
        return ResponseEntity.ok(ApiResponses.of("NOTIF-1400", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "NOTIF-1405 · Unread count")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountPayload>> unreadCount() {
        UnreadCountPayload data = notificationService.unreadCount();
        return ResponseEntity.ok(ApiResponses.of("NOTIF-1405", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "NOTIF-1440 · Real-time notification stream (SSE)")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        User me = SecurityUtils.requireCurrentUser();
        SseEmitter emitter = notificationSseHub.subscribe(me.getUuid());
        long unread = notificationService.unreadCount().getCount();
        notificationSseHub.publishUnreadCount(me.getUuid(), unread);
        return emitter;
    }

    @Operation(summary = "NOTIF-1410 · Mark notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationPayload>> markRead(@PathVariable Long id) {
        NotificationPayload data = notificationService.markRead(id);
        return ResponseEntity.ok(ApiResponses.of("NOTIF-1410", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "NOTIF-1420 · Mark all notifications as read")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationService.markAllRead();
        User me = SecurityUtils.requireCurrentUser();
        notificationSseHub.publishUnreadCount(me.getUuid(), 0);
        return ResponseEntity.ok(ApiResponses.of("NOTIF-1420", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "NOTIF-1430 · Delete notification")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        notificationService.delete(id);
        User me = SecurityUtils.requireCurrentUser();
        long unread = notificationService.unreadCount().getCount();
        notificationSseHub.publishUnreadCount(me.getUuid(), unread);
        return ResponseEntity.ok(ApiResponses.of("NOTIF-1430", LmsStatusCode.SUCCESS, null, null));
    }
}
