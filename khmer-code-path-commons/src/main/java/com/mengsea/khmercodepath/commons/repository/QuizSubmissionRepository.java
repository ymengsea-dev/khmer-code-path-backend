package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    @Query("""
            SELECT COUNT(s) FROM QuizSubmission s
            WHERE s.student.uuid = :studentUuid
            AND s.status IN ('SUBMITTED', 'COMPLETED')
            """)
    long countCompletedByStudentUuid(@Param("studentUuid") String studentUuid);

    @Query("""
            SELECT s FROM QuizSubmission s
            JOIN FETCH s.quiz q
            JOIN FETCH q.lmsClass c
            WHERE s.student.uuid = :studentUuid
            ORDER BY s.submittedAt DESC
            """)
    List<QuizSubmission> findByStudentUuidWithQuiz(@Param("studentUuid") String studentUuid);

    java.util.Optional<QuizSubmission> findByQuiz_IdAndStudent_Uuid(Long quizId, String studentUuid);

    boolean existsByQuiz_IdAndStudent_Uuid(Long quizId, String studentUuid);

    long countByQuiz_Id(Long quizId);

    long countByQuiz_IdAndStatus(Long quizId, String status);

    @Query("""
            SELECT COUNT(s) FROM QuizSubmission s
            WHERE s.quiz.id = :quizId
            AND s.status IN ('SUBMITTED', 'COMPLETED')
            """)
    long countSubmittedByQuizId(@Param("quizId") Long quizId);

    @Query("""
            SELECT s FROM QuizSubmission s
            JOIN FETCH s.student
            JOIN FETCH s.quiz q
            WHERE q.id = :quizId
            ORDER BY s.submittedAt DESC
            """)
    List<QuizSubmission> findByQuizIdWithStudent(@Param("quizId") Long quizId);
}
