package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    @Query("""
            SELECT AVG(g.numericGrade) FROM StudentGrade g
            WHERE g.student.uuid = :studentUuid
            AND g.numericGrade IS NOT NULL
            """)
    BigDecimal averageNumericGradeByStudentUuid(@Param("studentUuid") String studentUuid);

    List<StudentGrade> findByLmsClass_IdOrderByStudent_UsernameAsc(Long classId);

    List<StudentGrade> findByStudent_UuidOrderByCreatedAtDesc(String studentUuid);

    List<StudentGrade> findByStudent_UuidAndLmsClass_IdOrderByCreatedAtDesc(
            String studentUuid,
            Long classId
    );

    Optional<StudentGrade> findFirstByLmsClass_IdAndStudent_UuidOrderByCreatedAtDesc(
            Long classId,
            String studentUuid
    );
}
