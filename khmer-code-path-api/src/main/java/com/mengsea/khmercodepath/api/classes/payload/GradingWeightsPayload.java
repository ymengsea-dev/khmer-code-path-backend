package com.mengsea.khmercodepath.api.classes.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradingWeightsPayload {
    private int attendance;
    private int assignment;
    private int quiz;
    private int midterm;
    private int finalExam;
}
