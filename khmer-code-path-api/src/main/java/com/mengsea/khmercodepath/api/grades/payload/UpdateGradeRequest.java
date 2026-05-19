package com.mengsea.khmercodepath.api.grades.payload;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateGradeRequest {

    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal numericGrade;

    private String letterGrade;
}
