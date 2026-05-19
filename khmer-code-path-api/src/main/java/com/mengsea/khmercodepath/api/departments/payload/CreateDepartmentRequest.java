package com.mengsea.khmercodepath.api.departments.payload;

import com.mengsea.khmercodepath.commons.constant.DepartmentAccent;
import com.mengsea.khmercodepath.commons.constant.DepartmentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String faculty;

    @Size(max = 255)
    private String headOfDept;

    private String hodId;

    @Min(0)
    private Integer facultyCount;

    @Min(0)
    @Max(100)
    private Integer capacityPercent;

    private DepartmentStatus status;

    private DepartmentAccent accent;
}
