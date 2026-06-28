package com.mengsea.khmercodepath.api.classes.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.classes")
public class ClassesProperties {

    private CreateDefaults createDefaults = new CreateDefaults();
    private GradingWeights gradingWeights = new GradingWeights();

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
}
