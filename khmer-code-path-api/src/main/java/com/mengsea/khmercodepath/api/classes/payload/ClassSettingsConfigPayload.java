package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassSettingsConfigPayload {
    private Long classId;
    private String className;
    private boolean publicCoursesEnabled;
    private List<String> allowedVisibilityValues;
    private List<com.mengsea.khmercodepath.api.departments.payload.DepartmentOptionPayload> departmentOptions;
}
