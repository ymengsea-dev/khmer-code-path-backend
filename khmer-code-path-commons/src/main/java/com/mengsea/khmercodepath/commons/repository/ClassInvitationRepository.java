package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.InvitationStatus;
import com.mengsea.khmercodepath.commons.domain.ClassInvitation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClassInvitationRepository extends JpaRepository<ClassInvitation, Long> {

    boolean existsByLmsClass_IdAndStudent_UuidAndStatus(
            Long classId,
            String studentUuid,
            InvitationStatus status
    );

    @EntityGraph(attributePaths = {"lmsClass", "lmsClass.teacher", "student", "invitedBy"})
    Optional<ClassInvitation> findByIdAndStudent_Uuid(Long id, String studentUuid);

    @EntityGraph(attributePaths = {"lmsClass", "lmsClass.teacher", "invitedBy"})
    List<ClassInvitation> findByStudent_UuidAndStatusOrderByCreatedAtDesc(
            String studentUuid,
            InvitationStatus status
    );

    @EntityGraph(attributePaths = {"student", "invitedBy"})
    List<ClassInvitation> findByLmsClass_IdAndStatusOrderByCreatedAtDesc(
            Long classId,
            InvitationStatus status
    );

    @Modifying
    @Query("""
            UPDATE ClassInvitation i SET i.status = :cancelled, i.respondedAt = CURRENT_TIMESTAMP
            WHERE i.lmsClass.id = :classId AND i.student.uuid IN :studentUuids
            AND i.status = :pending
            """)
    int cancelPendingForStudents(
            @Param("classId") Long classId,
            @Param("studentUuids") Collection<String> studentUuids,
            @Param("pending") InvitationStatus pending,
            @Param("cancelled") InvitationStatus cancelled
    );
}
