package com.mengsea.khmercodepath.api.notifications.payload;

import com.mengsea.khmercodepath.commons.constant.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotificationPayload {
    Long id;
    NotificationType type;
    String title;
    String message;
    Long classId;
    String className;
    String resourceType;
    Long resourceId;
    boolean read;
    LocalDateTime createdAt;
}
