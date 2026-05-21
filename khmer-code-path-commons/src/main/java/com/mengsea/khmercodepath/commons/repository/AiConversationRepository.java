package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.AiSectionType;
import com.mengsea.khmercodepath.commons.domain.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, String> {

    Optional<AiConversation> findByIdAndUser_UuidAndDeletedFalse(String id, String userUuid);

    List<AiConversation> findByUser_UuidAndDeletedFalseOrderByUpdatedAtDesc(String userUuid);

    List<AiConversation> findByUser_UuidAndSectionTypeAndSectionRefAndDeletedFalseOrderByUpdatedAtDesc(
            String userUuid,
            AiSectionType sectionType,
            String sectionRef
    );
}
