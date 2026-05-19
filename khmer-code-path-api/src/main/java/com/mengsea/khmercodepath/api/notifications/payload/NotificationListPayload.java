package com.mengsea.khmercodepath.api.notifications.payload;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class NotificationListPayload {
    List<NotificationPayload> items;
    long total;
    long unreadCount;
}
