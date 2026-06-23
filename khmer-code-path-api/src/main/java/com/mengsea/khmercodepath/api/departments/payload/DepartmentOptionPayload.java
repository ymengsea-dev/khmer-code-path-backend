package com.mengsea.khmercodepath.api.departments.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentOptionPayload {
    private Long id;
    private String name;
    private Long facultyId;
    private String facultyName;
}
