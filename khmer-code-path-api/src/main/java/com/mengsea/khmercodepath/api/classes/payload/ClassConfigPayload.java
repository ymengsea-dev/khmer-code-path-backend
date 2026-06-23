package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassConfigPayload {
    private String allSemestersLabel;
    private List<SemesterFilterPayload> semesterFilters;
    private List<LessonTabPayload> lessonTabs;
    private List<String> cardGradients;
    private ClassCreateDefaultsPayload createDefaults;
    private GradingWeightsPayload gradingWeights;
    private List<ScoreComponentPayload> scoreComponents;
    private List<com.mengsea.khmercodepath.api.departments.payload.DepartmentOptionPayload> departmentOptions;
}
