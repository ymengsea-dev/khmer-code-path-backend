package com.mengsea.khmercodepath.api.users.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserManagementConfigPayload {
    private String pageTitle;
    private String pageDescription;
    private List<UserTabPayload> tabs;
    private List<StatusFilterPayload> statusFilters;
    private List<ClassFilterPayload> classFilters;
    private List<String> cardGradients;
    private UserManagementActionsPayload actions;
}
