package com.mengsea.khmercodepath.api.classes.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCoursesConfigPayload {
    private String pageTitle;
    private String pageDescription;
    private String navLabel;
    private String emptyMessage;
    private String enrollButtonLabel;
    private String enrolledLabel;
    private String searchPlaceholder;
    private boolean enabled;
}
