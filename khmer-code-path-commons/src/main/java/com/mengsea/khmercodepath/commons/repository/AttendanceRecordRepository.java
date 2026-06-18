package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE a.student.uuid = :studentUuid
            """)
    long countByStudentUuid(@Param("studentUuid") String studentUuid);

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE a.student.uuid = :studentUuid
            AND a.status = 'PRESENT'
            """)
    long countPresentByStudentUuid(@Param("studentUuid") String studentUuid);

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE a.student.uuid = :studentUuid
            AND (:classId IS NULL OR a.lmsClass.id = :classId)
            AND a.status = 'PRESENT'
            """)
    long countPresentByStudentUuidAndClass(
            @Param("studentUuid") String studentUuid,
            @Param("classId") Long classId
    );

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE a.student.uuid = :studentUuid
            AND (:classId IS NULL OR a.lmsClass.id = :classId)
            """)
    long countByStudentUuidAndClass(
            @Param("studentUuid") String studentUuid,
            @Param("classId") Long classId
    );

    List<AttendanceRecord> findByLmsClass_IdAndSessionDateOrderByStudent_UsernameAsc(
            Long classId,
            LocalDate sessionDate
    );

    List<AttendanceRecord> findByStudent_UuidOrderBySessionDateDesc(String studentUuid);

    List<AttendanceRecord> findByStudent_UuidAndLmsClass_IdOrderBySessionDateDesc(
            String studentUuid,
            Long classId
    );

    Optional<AttendanceRecord> findByLmsClass_IdAndStudent_UuidAndSessionDate(
            Long classId,
            String studentUuid,
            LocalDate sessionDate
    );

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE (:classId IS NULL OR a.lmsClass.id = :classId)
            AND (:studentUuid IS NULL OR a.student.uuid = :studentUuid)
            AND a.status = :status
            """)
    long countByFiltersAndStatus(
            @Param("classId") Long classId,
            @Param("studentUuid") String studentUuid,
            @Param("status") String status
    );

    List<AttendanceRecord> findByLmsClass_IdOrderBySessionDateAscStudent_UsernameAsc(Long classId);

    @Query("""
            SELECT a FROM AttendanceRecord a
            WHERE a.lmsClass.id = :classId
            AND a.sessionDate >= :dateFrom
            AND a.sessionDate <= :dateTo
            ORDER BY a.sessionDate ASC, a.student.username ASC
            """)
    List<AttendanceRecord> findByClassIdAndSessionDateBetween(
            @Param("classId") Long classId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
            SELECT DISTINCT a.sessionDate FROM AttendanceRecord a
            WHERE a.lmsClass.id = :classId
            ORDER BY a.sessionDate DESC
            """)
    List<LocalDate> findDistinctSessionDatesByClassId(@Param("classId") Long classId);

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE a.lmsClass.id = :classId
            AND a.student.uuid = :studentUuid
            AND a.status = :status
            AND a.sessionDate >= :dateFrom
            AND a.sessionDate <= :dateTo
            """)
    long countByClassStudentStatusAndSessionDateBetween(
            @Param("classId") Long classId,
            @Param("studentUuid") String studentUuid,
            @Param("status") String status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );
}
