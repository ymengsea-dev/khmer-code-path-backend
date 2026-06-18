package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreComponentPayload {
    private String key;
    private String label;
    /** UI color token, e.g. emerald, blue, violet, amber, rose */
    private String color;
}
