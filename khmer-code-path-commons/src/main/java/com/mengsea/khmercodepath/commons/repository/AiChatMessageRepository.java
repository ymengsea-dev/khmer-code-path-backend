package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findByConversation_IdOrderByCreatedAtAsc(String conversationId);

    void deleteByConversation_Id(String conversationId);

    long countByConversation_Id(String conversationId);
}
