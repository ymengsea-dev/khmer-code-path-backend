package com.mengsea.khmercodepath.api.faculties.payload;

import com.mengsea.khmercodepath.commons.constant.FacultyStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultySummaryPayload {
    private Long id;
    private String name;
    private String tagline;
    private String coverUrl;
    private FacultyStatus status;
    private int departmentCount;
}
