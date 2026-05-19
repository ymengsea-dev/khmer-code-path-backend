package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("""
            SELECT COUNT(q) FROM Quiz q
            WHERE q.deleted = false
            AND q.lmsClass.deleted = false
            AND q.lmsClass.teacher.uuid = :teacherUuid
            """)
    long countByTeacherUuid(@Param("teacherUuid") String teacherUuid);
}
