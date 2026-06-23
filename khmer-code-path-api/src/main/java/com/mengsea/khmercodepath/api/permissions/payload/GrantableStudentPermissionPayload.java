package com.mengsea.khmercodepath.api.permissions.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrantableStudentPermissionPayload {
    private String authority;
    private String label;
    private String description;
    private boolean defaultForStudent;
}
