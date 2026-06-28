package com.mengsea.khmercodepath.api.exam.payload;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ExamQuestionDto {
    Long id;
    String question;
    List<String> options;
    Integer correctIndex;
    String explanation;
}
