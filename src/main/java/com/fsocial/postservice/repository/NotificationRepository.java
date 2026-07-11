package com.fsocial.postservice.repository;

import com.fsocial.postservice.dto.notification.NotificationResponse;
import com.fsocial.postservice.entity.Notification;
import com.fsocial.postservice.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    long countByRecipientIdAndIsReadFalse(String recipientId);

    List<NotificationResponse> findByRecipientIdOrderByIdDesc(String recipientId, Pageable pageable);

    List<NotificationResponse> findByRecipientIdAndIdLessThanOrderByIdDesc(
            String recipientId, String id, Pageable pageable);

    Optional<Notification> findFirstByRecipientIdAndGroupKeyAndIsReadFalseAndCreatedAtAfter(
            String recipientId, String groupKey, Instant since);
}
