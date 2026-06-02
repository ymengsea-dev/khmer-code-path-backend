package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuiz_IdOrderByOrderIndex(Long quizId);
}
