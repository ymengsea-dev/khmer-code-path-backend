package com.mengsea.khmercodepath.api.permissions.payload;

import com.mengsea.khmercodepath.commons.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionsPayload {
    private Role role;
    private List<TeacherPermissionStatePayload> permissions;
}
