package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SemesterFilterPayload {
    /** Value sent back as semester filter (label shown in UI). */
    private String value;
    private String label;
    private String semester;
    private Integer academicYear;
}
