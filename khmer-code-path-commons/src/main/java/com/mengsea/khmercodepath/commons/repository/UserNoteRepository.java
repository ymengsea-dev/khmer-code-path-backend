package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserNoteRepository extends JpaRepository<UserNote, Long> {

    Optional<UserNote> findByIdAndUser_UuidAndDeletedFalse(Long id, String userUuid);

    List<UserNote> findByUser_UuidAndDeletedFalseOrderByUpdatedAtDesc(String userUuid);

    @Query("""
            SELECT n FROM UserNote n
            WHERE n.user.uuid = :userUuid AND n.deleted = false
            AND (
                LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(n.preview) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            ORDER BY n.updatedAt DESC
            """)
    List<UserNote> searchByUser(@Param("userUuid") String userUuid, @Param("search") String search);
}
