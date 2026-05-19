package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    long countByLmsClass_Id(Long classId);

    long countByLmsClass_IdAndDeletedFalse(Long classId);

    @Query("""
            SELECT COUNT(l) FROM Lesson l
            WHERE l.lmsClass.id = :classId AND l.deleted = false
            """)
    long countActiveByClassId(@Param("classId") Long classId);

    List<Lesson> findByLmsClass_IdAndDeletedFalseOrderBySortOrderAscCreatedAtAsc(Long classId);

    Optional<Lesson> findByIdAndDeletedFalse(Long id);

    @Query("""
            SELECT l FROM Lesson l
            JOIN FETCH l.lmsClass c
            WHERE l.id = :id AND l.deleted = false AND c.deleted = false
            """)
    Optional<Lesson> findByIdAndDeletedFalseWithClass(@Param("id") Long id);
}
