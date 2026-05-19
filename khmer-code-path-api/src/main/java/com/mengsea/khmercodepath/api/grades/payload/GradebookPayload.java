package com.mengsea.khmercodepath.api.grades.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GradebookPayload {
    private Long classId;
    private String className;
    private List<GradebookRowPayload> rows;
}
