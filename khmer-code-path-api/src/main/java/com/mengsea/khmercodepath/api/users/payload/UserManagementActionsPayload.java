package com.mengsea.khmercodepath.api.users.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserManagementActionsPayload {
    private boolean canAdd;
    private boolean canImport;
    private boolean canEditStatus;
}
