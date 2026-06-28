package com.mengsea.khmercodepath.api.ai.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AiStatusPayload {
    boolean enabled;
    boolean available;
    String provider;
    String baseUrl;
}
