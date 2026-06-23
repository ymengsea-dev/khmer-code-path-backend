package com.mengsea.khmercodepath.api.permissions.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPermissionsPayload {
    private String userId;
    private String name;
    private String email;
    private List<TeacherPermissionStatePayload> permissions;
}
