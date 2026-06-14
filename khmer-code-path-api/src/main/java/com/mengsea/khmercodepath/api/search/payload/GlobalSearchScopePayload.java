package com.mengsea.khmercodepath.api.search.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalSearchScopePayload {
    private String id;
    private String label;
    private String placeholder;
    private String icon;
}
