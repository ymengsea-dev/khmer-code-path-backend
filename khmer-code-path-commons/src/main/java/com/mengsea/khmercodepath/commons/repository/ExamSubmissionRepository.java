package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {

    Optional<ExamSubmission> findByExam_IdAndStudent_Uuid(Long examId, String studentUuid);

    boolean existsByExam_IdAndStudent_Uuid(Long examId, String studentUuid);

    long countByExam_Id(Long examId);

    long countByExam_IdAndStatus(Long examId, String status);

    @Query("""
            SELECT COUNT(s) FROM ExamSubmission s
            WHERE s.exam.id = :examId
            AND s.status IN ('SUBMITTED', 'COMPLETED')
            """)
    long countSubmittedByExamId(@Param("examId") Long examId);

    @Query("""
            SELECT s FROM ExamSubmission s
            JOIN FETCH s.student
            JOIN FETCH s.exam e
            WHERE e.id = :examId
            ORDER BY s.submittedAt DESC
            """)
    List<ExamSubmission> findByExamIdWithStudent(@Param("examId") Long examId);
}
