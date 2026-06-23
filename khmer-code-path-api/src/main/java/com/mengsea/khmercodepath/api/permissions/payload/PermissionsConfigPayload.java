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
public class PermissionsConfigPayload {
    private String pageTitle;
    private String pageDescription;
    private String selectTeacherLabel;
    private String saveButtonLabel;
    private String teacherSectionTitle;
    private String teacherSectionDescription;
    private String studentSectionTitle;
    private String studentSectionDescription;
    private String rolesSectionTitle;
    private String rolesSectionDescription;
    private String roleColumnLabel;
    private String statusColumnLabel;
    private String schoolFeaturesSectionTitle;
    private String schoolFeaturesSectionDescription;
    private String publicCoursesFeatureLabel;
    private String publicCoursesFeatureDescription;
    private List<PermissionsTabPayload> tabs;
    private List<AssignableRolePayload> assignableRoles;
    private List<RoleSummaryPayload> roleSummaries;
    private List<GrantablePermissionPayload> grantablePermissions;
    private List<GrantableStudentPermissionPayload> grantableStudentPermissions;
}
