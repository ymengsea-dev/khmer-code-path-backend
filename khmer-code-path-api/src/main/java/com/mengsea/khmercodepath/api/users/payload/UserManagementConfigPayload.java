package com.mengsea.khmercodepath.api.users.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserManagementConfigPayload {
    private List<ClassFilterPayload> classFilters;
    private UserManagementActionsPayload actions;
}
