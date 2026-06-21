package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Notification;
import com.fsocial.postservice.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(
            String recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(
            String recipientId, Pageable pageable);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(String recipientId, NotificationType type);

    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(String recipientId, NotificationType type, Pageable pageable);

    List<Notification> findByTypeAndPushedFalseAndExaminationTimeBetween(
            NotificationType type, LocalDateTime from, LocalDateTime to);

    long countByRecipientIdAndIsReadFalse(String recipientId);

    Optional<Notification> findFirstByRecipientIdAndGroupKeyAndIsReadFalseAndCreatedAtAfter(
            String recipientId, String groupKey, Instant since);
}
