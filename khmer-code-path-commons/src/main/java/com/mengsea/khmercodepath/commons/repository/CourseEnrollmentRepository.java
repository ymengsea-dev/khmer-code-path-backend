package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    Optional<CourseEnrollment> findByCourse_IdAndStudent_Uuid(Long courseId, String studentUuid);

    List<CourseEnrollment> findByStudent_Uuid(String studentUuid);
}
