package com.mengsea.khmercodepath.api.notifications.service;

import com.mengsea.khmercodepath.api.notifications.payload.NotificationListPayload;
import com.mengsea.khmercodepath.api.notifications.payload.NotificationPayload;
import com.mengsea.khmercodepath.api.notifications.payload.UnreadCountPayload;
import com.mengsea.khmercodepath.commons.constant.NotificationType;

public interface NotificationService {

    NotificationListPayload list(String filter, int limit, int offset);

    UnreadCountPayload unreadCount();

    NotificationPayload markRead(Long id);

    void markAllRead();

    void delete(Long id);

    NotificationPayload createForUser(
            String userUuid,
            NotificationType type,
            String title,
            String message,
            Long classId,
            String resourceType,
            Long resourceId
    );
}
