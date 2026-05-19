package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.LessonMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {

    List<LessonMaterial> findByLesson_IdAndDeletedFalseOrderByCreatedAtAsc(Long lessonId);

    long countByLesson_IdAndDeletedFalse(Long lessonId);

    Optional<LessonMaterial> findByIdAndDeletedFalse(Long id);
}
