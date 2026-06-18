package com.mengsea.khmercodepath.api.users.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusFilterPayload {
    private String value;
    private String label;
}
