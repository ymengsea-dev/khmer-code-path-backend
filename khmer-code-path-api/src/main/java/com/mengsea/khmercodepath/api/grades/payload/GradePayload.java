package com.mengsea.khmercodepath.api.grades.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GradePayload {
    private Long id;
    private Long classId;
    private String className;
    private String studentId;
    private String studentName;
    private BigDecimal numericGrade;
    private String letterGrade;
    private LocalDateTime createdAt;
}
