package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {

    long countByLmsClass_Id(Long classId);

    @EntityGraph(attributePaths = "student")
    List<ClassEnrollment> findByLmsClass_IdOrderByEnrolledAtAsc(Long classId);

    void deleteByLmsClass_IdAndStudent_UuidIn(Long classId, Collection<String> studentUuids);

    boolean existsByLmsClass_IdAndStudent_Uuid(Long classId, String studentUuid);
}
