package com.mengsea.khmercodepath.api.departments.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DepartmentDetailPayload {
    private DepartmentSummaryPayload department;
    private List<String> assignedTeachers;
}
