package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    long countByLmsClass_Id(Long classId);
}
