package com.mengsea.khmercodepath.api.classes.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.classes")
public class ClassesProperties {

    private String allSemestersLabel = "All Semesters";
    private List<String> cardGradients = defaultGradients();
    private List<LessonTabEntry> lessonTabs = defaultLessonTabs();
    private CreateDefaults createDefaults = new CreateDefaults();
    private GradingWeights gradingWeights = new GradingWeights();
    private List<SettingsTabEntry> settingsTabs = defaultSettingsTabs();
    private List<ScoreComponentEntry> scoreComponents = defaultScoreComponents();
    private List<VisibilityOptionEntry> visibilityOptions = defaultVisibilityOptions();
    private PublicCourses publicCourses = new PublicCourses();

    @Getter
    @Setter
    public static class VisibilityOptionEntry {
        private String value;
        private String label;
        private String description;
    }

    @Getter
    @Setter
    public static class PublicCourses {
        private String pageTitle = "Public Courses";
        private String pageDescription = "Browse open classes at your school and join instantly.";
        private String navLabel = "Public Courses";
        private String emptyMessage = "No public courses are available right now.";
        private String enrollButtonLabel = "Join class";
        private String enrolledLabel = "Enrolled";
        private String searchPlaceholder = "Search public courses…";
        private String disabledHint =
                "Your school administrator has not enabled public courses. Only private (invite-only) classes are available.";
    }

    @Getter
    @Setter
    public static class SettingsTabEntry {
        private String id;
        private String label;
    }

    @Getter
    @Setter
    public static class ScoreComponentEntry {
        private String key;
        private String label;
        private String color;
    }

    @Getter
    @Setter
    public static class LessonTabEntry {
        private String id;
        private String label;
    }

    @Getter
    @Setter
    public static class CreateDefaults {
        private String semester = "Semester 1";
        private int academicYear = 2026;
    }

    @Getter
    @Setter
    public static class GradingWeights {
        private int attendance = 10;
        private int assignment = 10;
        private int quiz = 5;
        private int midterm = 25;
        private int finalExam = 50;
    }

    private static List<String> defaultGradients() {
        return List.of(
                "from-indigo-500 to-purple-600",
                "from-blue-600 to-sky-700",
                "from-emerald-600 to-teal-700",
                "from-amber-500 to-orange-600",
                "from-rose-500 to-pink-600",
                "from-violet-600 to-fuchsia-700"
        );
    }

    private static List<LessonTabEntry> defaultLessonTabs() {
        List<LessonTabEntry> tabs = new ArrayList<>();
        tabs.add(tab("content", "Lessons"));
        tabs.add(tab("materials", "Materials"));
        tabs.add(tab("ai", "AI"));
        tabs.add(tab("comments", "Comments"));
        return tabs;
    }

    private static LessonTabEntry tab(String id, String label) {
        LessonTabEntry e = new LessonTabEntry();
        e.setId(id);
        e.setLabel(label);
        return e;
    }

    private static List<SettingsTabEntry> defaultSettingsTabs() {
        List<SettingsTabEntry> tabs = new ArrayList<>();
        tabs.add(settingsTab("general", "General"));
        tabs.add(settingsTab("grading", "Score breakdown"));
        tabs.add(settingsTab("students", "Students"));
        tabs.add(settingsTab("lessons", "Lessons"));
        tabs.add(settingsTab("quizzes", "Quizzes"));
        return tabs;
    }

    private static SettingsTabEntry settingsTab(String id, String label) {
        SettingsTabEntry e = new SettingsTabEntry();
        e.setId(id);
        e.setLabel(label);
        return e;
    }

    private static List<ScoreComponentEntry> defaultScoreComponents() {
        List<ScoreComponentEntry> items = new ArrayList<>();
        items.add(scoreComponent("attendance", "Attendance", "emerald"));
        items.add(scoreComponent("assignment", "Assignment", "blue"));
        items.add(scoreComponent("quiz", "Quiz", "violet"));
        items.add(scoreComponent("midterm", "Mid-term", "amber"));
        items.add(scoreComponent("finalExam", "Final", "rose"));
        return items;
    }

    private static ScoreComponentEntry scoreComponent(String key, String label, String color) {
        ScoreComponentEntry e = new ScoreComponentEntry();
        e.setKey(key);
        e.setLabel(label);
        e.setColor(color);
        return e;
    }

    private static List<VisibilityOptionEntry> defaultVisibilityOptions() {
        List<VisibilityOptionEntry> items = new ArrayList<>();
        items.add(visibilityOption("PRIVATE", "Private (invite only)",
                "Students can join only after you invite them."));
        items.add(visibilityOption("PUBLIC", "Public (self-enroll)",
                "Students at your school can discover and join this class from Public Courses."));
        return items;
    }

    private static VisibilityOptionEntry visibilityOption(String value, String label, String description) {
        VisibilityOptionEntry e = new VisibilityOptionEntry();
        e.setValue(value);
        e.setLabel(label);
        e.setDescription(description);
        return e;
    }
}
