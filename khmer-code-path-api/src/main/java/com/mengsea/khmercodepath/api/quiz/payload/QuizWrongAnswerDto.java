package com.mengsea.khmercodepath.api.quiz.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuizWrongAnswerDto {
    Long questionId;
    String question;
    Integer selectedIndex;
    String selectedAnswer;
    Integer correctIndex;
    String correctAnswer;
    String explanation;
}
