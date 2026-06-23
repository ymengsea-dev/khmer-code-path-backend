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
    private Long facultyId;
    private String facultyName;
    private String headOfDept;
    private int teacherCount;
    private int classCount;
    private int capacityPercent;
    private DepartmentStatus status;
    private DepartmentAccent accent;
}
