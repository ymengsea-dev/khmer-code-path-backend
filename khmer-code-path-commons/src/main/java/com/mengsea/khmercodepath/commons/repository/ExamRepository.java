package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query("""
            SELECT e FROM Exam e
            JOIN FETCH e.lmsClass c
            WHERE e.deleted = false
            AND c.deleted = false
            AND c.teacher.uuid = :teacherUuid
            ORDER BY e.createdAt DESC
            """)
    List<Exam> findAllByTeacherUuid(@Param("teacherUuid") String teacherUuid);

    @Query("""
            SELECT e FROM Exam e
            JOIN FETCH e.lmsClass c
            WHERE e.deleted = false
            AND c.deleted = false
            AND c.id = :classId
            AND c.teacher.uuid = :teacherUuid
            ORDER BY e.createdAt DESC
            """)
    List<Exam> findByClassAndTeacher(@Param("classId") Long classId, @Param("teacherUuid") String teacherUuid);

    @Query("""
            SELECT DISTINCT e FROM Exam e
            JOIN FETCH e.lmsClass c
            WHERE e.deleted = false
            AND c.deleted = false
            AND e.status = 'PUBLISHED'
            AND EXISTS (
                SELECT en FROM ClassEnrollment en
                WHERE en.lmsClass.id = c.id
                AND en.student.uuid = :studentUuid
            )
            ORDER BY e.createdAt DESC
            """)
    List<Exam> findPublishedForStudent(@Param("studentUuid") String studentUuid);

    Optional<Exam> findByIdAndDeletedFalse(Long id);

    @Query("""
            SELECT e FROM Exam e
            WHERE e.deleted = false
            AND e.status = 'PUBLISHED'
            AND e.lmsClass.id = :classId
            ORDER BY e.createdAt DESC
            """)
    List<Exam> findPublishedByClassId(@Param("classId") Long classId);
}
