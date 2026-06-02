package com.mengsea.khmercodepath.api.lessons.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryViewPayload {
    private String id;
    private String label;
    private String searchPlaceholder;
}
