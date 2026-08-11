package com.fsocial.repository;

import com.fsocial.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    List<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    List<Message> findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            String conversationId, java.time.LocalDateTime before, Pageable pageable);

    Message findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);
}
