package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExam_IdOrderByOrderIndex(Long examId);

    void deleteByExam_Id(Long examId);
}
