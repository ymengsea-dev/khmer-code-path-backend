package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassCreateDefaultsPayload {
    private String semester;
    private int academicYear;
}
