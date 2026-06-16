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
}
