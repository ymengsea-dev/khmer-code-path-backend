package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_UuidAndDeletedFalseOrderByCreatedAtDesc(
            String userUuid,
            Pageable pageable
    );

    Page<Notification> findByUser_UuidAndDeletedFalseAndReadOrderByCreatedAtDesc(
            String userUuid,
            boolean read,
            Pageable pageable
    );

    long countByUser_UuidAndDeletedFalseAndReadFalse(String userUuid);

    Optional<Notification> findByIdAndUser_UuidAndDeletedFalse(Long id, String userUuid);

    @Modifying
    @Query("""
            UPDATE Notification n SET n.read = true
            WHERE n.user.uuid = :userUuid AND n.deleted = false AND n.read = false
            """)
    int markAllReadForUser(@Param("userUuid") String userUuid);
}
