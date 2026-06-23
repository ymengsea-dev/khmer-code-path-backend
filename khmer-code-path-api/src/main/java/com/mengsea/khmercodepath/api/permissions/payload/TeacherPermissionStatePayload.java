package com.mengsea.khmercodepath.api.permissions.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPermissionStatePayload {
    private String authority;
    private String label;
    private String description;
    private boolean defaultGranted;
    private boolean granted;
    private boolean overridden;
}
