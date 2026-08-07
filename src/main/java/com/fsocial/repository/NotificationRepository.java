package com.fsocial.repository;

import com.fsocial.entity.Notification;
import com.fsocial.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(
            String recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(
            String recipientId, Pageable pageable);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(String recipientId, NotificationType type);

    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(String recipientId, NotificationType type, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(String recipientId);

    /**
     * Trang đầu của cursor pagination. Sắp theo (created_at, id) chứ không theo id:
     * id là UUID ngẫu nhiên nên không có thứ tự thời gian.
     */
    @Query("select n from Notification n where n.recipientId = :recipientId order by n.createdAt desc, n.id desc")
    List<Notification> findFirstPage(@Param("recipientId") String recipientId, Pageable pageable);

    /** Trang kế — keyset trên (created_at, id). */
    @Query("""
            select n from Notification n
            where n.recipientId = :recipientId
              and (n.createdAt < :cursorAt
                   or (n.createdAt = :cursorAt and n.id < :cursorId))
            order by n.createdAt desc, n.id desc
            """)
    List<Notification> findNextPage(@Param("recipientId") String recipientId,
                                    @Param("cursorAt") Instant cursorAt,
                                    @Param("cursorId") String cursorId,
                                    Pageable pageable);

    Optional<Notification> findFirstByRecipientIdAndGroupKeyAndIsReadFalseAndCreatedAtAfter(
            String recipientId, String groupKey, Instant since);
}
