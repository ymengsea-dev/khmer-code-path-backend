package com.mengsea.khmercodepath.api.progress.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClassProgressPayload {
    private Long classId;
    private String className;
    private String classCode;
    private BigDecimal numericGrade;
    private String letterGrade;
    private double attendanceRate;
    private long quizzesCompleted;
    private boolean completed;
}
