package com.mengsea.khmercodepath.api.grades.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GradebookRowPayload {
    private String studentId;
    private String studentName;
    private Long gradeId;
    private BigDecimal numericGrade;
    private String letterGrade;
}
