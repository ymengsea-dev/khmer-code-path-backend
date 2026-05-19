package com.mengsea.khmercodepath.api.grades.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FinalGradePayload {
    private Long classId;
    private String studentId;
    private BigDecimal numericGrade;
    private String letterGrade;
}
