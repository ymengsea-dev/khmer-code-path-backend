package com.mengsea.khmercodepath.api.quiz.payload;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class QuizQuestionDto {
    Long id;
    String question;
    List<String> options;
    /** Null when returned to students (correct answer hidden until submission). */
    Integer correctIndex;
    String explanation;
}
