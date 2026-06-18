package com.mengsea.khmercodepath.api.users.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTabPayload {
    private String id;
    private String label;
}
