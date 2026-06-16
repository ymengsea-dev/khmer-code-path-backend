package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("""
            SELECT COUNT(q) FROM Quiz q
            WHERE q.deleted = false
            AND q.lmsClass.deleted = false
            AND q.lmsClass.teacher.uuid = :teacherUuid
            """)
    long countByTeacherUuid(@Param("teacherUuid") String teacherUuid);

    /** Teacher: list all published/draft quizzes across all their classes. */
    @Query("""
            SELECT q FROM Quiz q
            JOIN FETCH q.lmsClass c
            WHERE q.deleted = false
            AND c.deleted = false
            AND c.teacher.uuid = :teacherUuid
            ORDER BY q.createdAt DESC
            """)
    List<Quiz> findAllByTeacherUuid(@Param("teacherUuid") String teacherUuid);

    /** Teacher: list quizzes for a specific class they own. */
    @Query("""
            SELECT q FROM Quiz q
            JOIN FETCH q.lmsClass c
            WHERE q.deleted = false
            AND c.deleted = false
            AND c.id = :classId
            AND c.teacher.uuid = :teacherUuid
            ORDER BY q.createdAt DESC
            """)
    List<Quiz> findByClassAndTeacher(@Param("classId") Long classId, @Param("teacherUuid") String teacherUuid);

    /** Student: list PUBLISHED quizzes for enrolled classes. */
    @Query("""
            SELECT DISTINCT q FROM Quiz q
            JOIN FETCH q.lmsClass c
            WHERE q.deleted = false
            AND c.deleted = false
            AND q.status = 'PUBLISHED'
            AND EXISTS (
                SELECT e FROM ClassEnrollment e
                WHERE e.lmsClass.id = c.id
                AND e.student.uuid = :studentUuid
            )
            ORDER BY q.createdAt DESC
            """)
    List<Quiz> findPublishedForStudent(@Param("studentUuid") String studentUuid);

    Optional<Quiz> findByIdAndDeletedFalse(Long id);

    /** Student: count PUBLISHED quizzes assigned to enrolled classes. */
    @Query("""
            SELECT COUNT(DISTINCT q) FROM Quiz q
            JOIN q.lmsClass c
            WHERE q.deleted = false
            AND c.deleted = false
            AND q.status = 'PUBLISHED'
            AND EXISTS (
                SELECT e FROM ClassEnrollment e
                WHERE e.lmsClass.id = c.id
                AND e.student.uuid = :studentUuid
            )
            """)
    long countPublishedForStudent(@Param("studentUuid") String studentUuid);

    /** Teacher: sum of questionCount across all their non-deleted quizzes. */
    @Query("""
            SELECT COALESCE(SUM(q.questionCount), 0) FROM Quiz q
            WHERE q.deleted = false
            AND q.lmsClass.deleted = false
            AND q.lmsClass.teacher.uuid = :teacherUuid
            """)
    long sumQuestionCountByTeacherUuid(@Param("teacherUuid") String teacherUuid);
}
