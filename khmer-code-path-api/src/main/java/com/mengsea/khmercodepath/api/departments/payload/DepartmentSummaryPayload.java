package com.mengsea.khmercodepath.api.departments.payload;

import com.mengsea.khmercodepath.commons.constant.DepartmentAccent;
import com.mengsea.khmercodepath.commons.constant.DepartmentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentSummaryPayload {
    private Long id;
    private String name;
    private String faculty;
    private String headOfDept;
    private int facultyCount;
    private int capacityPercent;
    private DepartmentStatus status;
    private DepartmentAccent accent;
}
