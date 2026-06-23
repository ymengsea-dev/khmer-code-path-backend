package com.mengsea.khmercodepath.api.permissions.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.permissions")
public class PermissionsProperties {

    private String pageTitle = "Roles & Permissions";
    private String pageDescription = "";
    private String selectTeacherLabel = "Select teacher";
    private String saveButtonLabel = "Save permissions";
    private String teacherSectionTitle = "Teacher permissions";
    private String teacherSectionDescription =
            "Toggle capabilities for all teachers at your school. Changes apply on their next request.";
    private String studentSectionTitle = "Student permissions";
    private String studentSectionDescription =
            "Toggle capabilities for all students at your school. Changes apply on their next request.";
    private String rolesSectionTitle = "School members";
    private String rolesSectionDescription =
            "View every user at your school and assign their role. Changes are saved immediately.";
    private String roleColumnLabel = "Role";
    private String statusColumnLabel = "Status";
    private String schoolFeaturesSectionTitle = "School features";
    private String schoolFeaturesSectionDescription =
            "Enable optional capabilities for your school. Teachers only see options that are turned on here.";
    private String publicCoursesFeatureLabel = "Public courses";
    private String publicCoursesFeatureDescription =
            "Allow teachers to mark classes as public so students can self-enroll from the Public Courses page.";
    private List<TabEntry> tabs = defaultTabs();
    private List<AssignableRoleEntry> assignableRoles = defaultAssignableRoles();
    private List<RoleSummaryEntry> roleSummaries = defaultRoleSummaries();
    private List<GrantableEntry> grantable = defaultGrantable();
    private List<StudentGrantableEntry> grantableStudents = defaultGrantableStudents();

    @Getter
    @Setter
    public static class StudentGrantableEntry {
        private String authority;
        private String label;
        private String description;
        private boolean defaultForStudent;
    }

    @Getter
    @Setter
    public static class TabEntry {
        private String id;
        private String label;
    }

    @Getter
    @Setter
    public static class AssignableRoleEntry {
        private String role;
        private String label;
    }

    @Getter
    @Setter
    public static class RoleSummaryEntry {
        private String role;
        private String title;
        private String description;
        private List<HighlightEntry> highlights = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class HighlightEntry {
        private String label;
        private boolean granted;
    }

    @Getter
    @Setter
    public static class GrantableEntry {
        private String authority;
        private String label;
        private String description;
        private boolean defaultForTeacher;
    }

    private static List<TabEntry> defaultTabs() {
        List<TabEntry> items = new ArrayList<>();
        items.add(tab("roles", "Roles"));
        items.add(tab("permissions", "Permissions"));
        return items;
    }

    private static List<AssignableRoleEntry> defaultAssignableRoles() {
        List<AssignableRoleEntry> items = new ArrayList<>();
        items.add(assignable("STUDENT", "Student"));
        items.add(assignable("TEACHER", "Teacher"));
        items.add(assignable("ADMIN", "Administrator"));
        return items;
    }

    private static TabEntry tab(String id, String label) {
        TabEntry entry = new TabEntry();
        entry.setId(id);
        entry.setLabel(label);
        return entry;
    }

    private static AssignableRoleEntry assignable(String role, String label) {
        AssignableRoleEntry entry = new AssignableRoleEntry();
        entry.setRole(role);
        entry.setLabel(label);
        return entry;
    }

    private static List<RoleSummaryEntry> defaultRoleSummaries() {
        List<RoleSummaryEntry> items = new ArrayList<>();
        items.add(roleSummary("ADMIN", "School Administrator",
                "Full control of the school tenant, members, and settings.",
                highlight("Manage users", true),
                highlight("School settings", true),
                highlight("Operations", true),
                highlight("Academic records", true)));
        items.add(roleSummary("TEACHER", "Teacher",
                "Teaches classes and supports students. Defaults can be customized below.",
                highlight("Manage classes", true),
                highlight("Lessons & quizzes", true),
                highlight("Grades & attendance", true),
                highlight("User management", false)));
        items.add(roleSummary("STUDENT", "Student",
                "Learns in enrolled classes and uses personal study tools.",
                highlight("View classes", true),
                highlight("Submit quizzes", true),
                highlight("AI chat & notebook", true),
                highlight("User management", false)));
        return items;
    }

    private static RoleSummaryEntry roleSummary(
            String role,
            String title,
            String description,
            HighlightEntry... highlights
    ) {
        RoleSummaryEntry entry = new RoleSummaryEntry();
        entry.setRole(role);
        entry.setTitle(title);
        entry.setDescription(description);
        entry.setHighlights(List.of(highlights));
        return entry;
    }

    private static HighlightEntry highlight(String label, boolean granted) {
        HighlightEntry entry = new HighlightEntry();
        entry.setLabel(label);
        entry.setGranted(granted);
        return entry;
    }

    private static List<GrantableEntry> defaultGrantable() {
        List<GrantableEntry> items = new ArrayList<>();
        items.add(grantable("lms:usr:manage", "Manage users",
                "Create, import, and edit student accounts.", false));
        items.add(grantable("lms:cls:manage", "Manage classes",
                "Create and configure classes, enrollments, and class settings.", true));
        items.add(grantable("lms:crs:manage", "Manage course catalog",
                "Create and edit courses in the catalog.", true));
        items.add(grantable("lms:lsn:manage", "Manage lessons",
                "Create lessons and upload class materials.", true));
        items.add(grantable("lms:att:manage", "Manage attendance",
                "Record and update student attendance.", true));
        items.add(grantable("lms:grd:manage", "Manage grades",
                "Record and update student grades.", true));
        items.add(grantable("lms:ai:ingest", "Index materials for AI",
                "Upload and index lesson files for AI retrieval.", true));
        items.add(grantable("lms:ops:manage", "School operations",
                "Access inventory, requests, and infrastructure tools.", false));
        items.add(grantable("lms:school:manage", "School settings",
                "Edit school profile, registration portal, and permissions.", false));
        return items;
    }

    private static GrantableEntry grantable(
            String authority,
            String label,
            String description,
            boolean defaultForTeacher
    ) {
        GrantableEntry entry = new GrantableEntry();
        entry.setAuthority(authority);
        entry.setLabel(label);
        entry.setDescription(description);
        entry.setDefaultForTeacher(defaultForTeacher);
        return entry;
    }

    private static List<StudentGrantableEntry> defaultGrantableStudents() {
        List<StudentGrantableEntry> items = new ArrayList<>();
        items.add(studentGrantable("lms:ai:chat", "AI Chat",
                "Use the AI learning assistant.", true));
        items.add(studentGrantable("lms:cls:read", "View classes",
                "Browse enrolled and available classes.", true));
        items.add(studentGrantable("lms:crs:read", "View course catalog",
                "Browse courses in the catalog.", true));
        items.add(studentGrantable("lms:dash:read", "View dashboard",
                "Access the personal learning dashboard.", true));
        items.add(studentGrantable("lms:prog:read", "View progress",
                "Track personal learning progress.", true));
        items.add(studentGrantable("lms:ai:ingest", "Index materials for AI",
                "Upload and index files for AI retrieval.", false));
        items.add(studentGrantable("lms:lsn:manage", "Manage lessons",
                "Create lessons and upload class materials.", false));
        items.add(studentGrantable("lms:cls:manage", "Manage classes",
                "Create and configure classes.", false));
        return items;
    }

    private static StudentGrantableEntry studentGrantable(
            String authority,
            String label,
            String description,
            boolean defaultForStudent
    ) {
        StudentGrantableEntry entry = new StudentGrantableEntry();
        entry.setAuthority(authority);
        entry.setLabel(label);
        entry.setDescription(description);
        entry.setDefaultForStudent(defaultForStudent);
        return entry;
    }
}
