package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Uuid(Long assignmentId, String studentUuid);

    boolean existsByAssignment_IdAndStudent_Uuid(Long assignmentId, String studentUuid);

    long countByAssignment_Id(Long assignmentId);

    @Query("""
            SELECT COUNT(s) FROM AssignmentSubmission s
            WHERE s.assignment.id = :assignmentId
            AND s.status = 'SUBMITTED'
            """)
    long countSubmittedByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("""
            SELECT s FROM AssignmentSubmission s
            JOIN FETCH s.student
            JOIN FETCH s.assignment a
            WHERE a.id = :assignmentId
            ORDER BY s.submittedAt DESC
            """)
    List<AssignmentSubmission> findByAssignmentIdWithStudent(@Param("assignmentId") Long assignmentId);
}
