package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassConfigPayload {
    private List<SemesterFilterPayload> semesterFilters;
    private ClassCreateDefaultsPayload createDefaults;
    private GradingWeightsPayload gradingWeights;
    private List<com.mengsea.khmercodepath.api.departments.payload.DepartmentOptionPayload> departmentOptions;
}
