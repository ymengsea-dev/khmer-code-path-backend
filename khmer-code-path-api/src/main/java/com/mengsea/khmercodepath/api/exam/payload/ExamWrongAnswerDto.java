package com.mengsea.khmercodepath.api.exam.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExamWrongAnswerDto {
    Long questionId;
    String question;
    Integer selectedIndex;
    String selectedAnswer;
    Integer correctIndex;
    String correctAnswer;
    String explanation;
}
