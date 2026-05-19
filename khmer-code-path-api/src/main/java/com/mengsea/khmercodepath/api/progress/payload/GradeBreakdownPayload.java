package com.mengsea.khmercodepath.api.progress.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GradeBreakdownPayload {
    private Long classId;
    private String course;
    private String quizzes;
    private String midterm;
    private String finalExam;
    private String attendance;
    private BigDecimal numericGrade;
    private String grade;
}
