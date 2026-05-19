package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.ClassComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassCommentRepository extends JpaRepository<ClassComment, Long> {

    @EntityGraph(attributePaths = {"author", "lmsClass"})
    List<ClassComment> findByLmsClass_IdAndDeletedFalseOrderByCreatedAtDesc(Long classId);

    long countByLmsClass_IdAndDeletedFalse(Long classId);

    @Query("""
            SELECT COUNT(c) FROM ClassComment c
            WHERE c.deleted = false
            AND c.lmsClass.deleted = false
            AND c.lmsClass.teacher.uuid = :teacherUuid
            """)
    long countByTeacherUuid(@Param("teacherUuid") String teacherUuid);

    @EntityGraph(attributePaths = {"author", "lmsClass"})
    @Query("""
            SELECT c FROM ClassComment c
            WHERE c.deleted = false
            AND c.lmsClass.deleted = false
            AND c.lmsClass.teacher.uuid = :teacherUuid
            ORDER BY c.createdAt DESC
            """)
    List<ClassComment> findRecentByTeacherUuid(
            @Param("teacherUuid") String teacherUuid,
            org.springframework.data.domain.Pageable pageable
    );
}
