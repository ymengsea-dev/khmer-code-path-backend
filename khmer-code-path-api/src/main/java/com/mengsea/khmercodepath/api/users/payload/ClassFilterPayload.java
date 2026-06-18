package com.mengsea.khmercodepath.api.users.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassFilterPayload {
    private String value;
    private String label;
}
