package com.mengsea.khmercodepath.api.grades.payload;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGradeRequest {

    @NotNull
    private Long classId;

    @NotBlank
    private String studentId;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal numericGrade;

    private String letterGrade;
}
