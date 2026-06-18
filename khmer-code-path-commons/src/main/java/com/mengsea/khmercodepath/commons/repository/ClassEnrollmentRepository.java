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

    java.util.Optional<ClassEnrollment> findByLmsClass_IdAndStudent_Uuid(Long classId, String studentUuid);

    boolean existsByStudent_UuidAndLmsClass_Teacher_Uuid(String studentUuid, String teacherUuid);

    @EntityGraph(attributePaths = {"student", "lmsClass"})
    List<ClassEnrollment> findByStudent_UuidOrderByEnrolledAtDesc(String studentUuid);

    long countByStudent_Uuid(String studentUuid);

    long countByStudent_UuidAndCompletedAtIsNotNull(String studentUuid);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COUNT(DISTINCT e.student.uuid) FROM ClassEnrollment e
            WHERE e.lmsClass.deleted = false
            AND e.lmsClass.teacher.uuid = :teacherUuid
            """)
    long countStudentsByTeacherUuid(@org.springframework.data.repository.query.Param("teacherUuid") String teacherUuid);
}
