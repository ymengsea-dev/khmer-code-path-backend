package com.mengsea.khmercodepath.api.permissions.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignableRolePayload {
    private String role;
    private String label;
}
