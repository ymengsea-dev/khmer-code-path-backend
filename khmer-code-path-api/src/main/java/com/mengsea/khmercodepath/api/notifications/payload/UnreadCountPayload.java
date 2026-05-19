package com.mengsea.khmercodepath.api.notifications.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UnreadCountPayload {
    long count;
}
