package com.mengsea.khmercodepath.api.search.payload;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GlobalSearchResultPayload {
    private String id;
    private String type;
    private String title;
    private String subtitle;
    private String targetView;
    private Map<String, String> targetParams;
}
