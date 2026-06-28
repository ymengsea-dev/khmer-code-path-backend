package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("""
            SELECT a FROM Assignment a
            JOIN FETCH a.lmsClass c
            WHERE a.deleted = false
            AND c.deleted = false
            AND c.teacher.uuid = :teacherUuid
            ORDER BY a.createdAt DESC
            """)
    List<Assignment> findAllByTeacherUuid(@Param("teacherUuid") String teacherUuid);

    @Query("""
            SELECT a FROM Assignment a
            JOIN FETCH a.lmsClass c
            WHERE a.deleted = false
            AND c.deleted = false
            AND c.id = :classId
            AND c.teacher.uuid = :teacherUuid
            ORDER BY a.createdAt DESC
            """)
    List<Assignment> findByClassAndTeacher(@Param("classId") Long classId, @Param("teacherUuid") String teacherUuid);

    @Query("""
            SELECT DISTINCT a FROM Assignment a
            JOIN FETCH a.lmsClass c
            WHERE a.deleted = false
            AND c.deleted = false
            AND a.status = 'PUBLISHED'
            AND EXISTS (
                SELECT e FROM ClassEnrollment e
                WHERE e.lmsClass.id = c.id
                AND e.student.uuid = :studentUuid
            )
            ORDER BY a.createdAt DESC
            """)
    List<Assignment> findPublishedForStudent(@Param("studentUuid") String studentUuid);

    Optional<Assignment> findByIdAndDeletedFalse(Long id);

    @Query("""
            SELECT a FROM Assignment a
            WHERE a.deleted = false
            AND a.status = 'PUBLISHED'
            AND a.lmsClass.id = :classId
            ORDER BY a.createdAt DESC
            """)
    List<Assignment> findPublishedByClassId(@Param("classId") Long classId);
}
