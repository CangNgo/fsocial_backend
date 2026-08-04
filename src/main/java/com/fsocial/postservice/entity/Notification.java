package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "notification")
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(name = "recipient_id", length = 36, nullable = false)
    private String recipientId;

    /** Reference — lookup Account theo id này khi đọc để lấy displayName/avatar */
    @Column(name = "sender_id", length = 36)
    private String senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    private NotificationType type;

    @Column(name = "group_key", length = 128)
    private String groupKey;

    /** Khi gom nhóm: list senderId gần nhất (giới hạn ~5 người) */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "notification_sender",
            joinColumns = @JoinColumn(name = "notification_id"))
    @OrderColumn(name = "ord")
    @Column(name = "sender_id", length = 36, nullable = false)
    @Builder.Default
    private List<String> aggregatedSenderIds = new ArrayList<>();

    @Column(name = "title", columnDefinition = "text")
    private String title;

    @Column(name = "body", columnDefinition = "text")
    private String body;

    /** Payload linh hoạt: deep link, FCM custom data, image urls... */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "notification_metadata",
            joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "meta_key", length = 64)
    @Column(name = "meta_value", columnDefinition = "text")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "read_at")
    private Instant readAt;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "pushed", nullable = false)
    private boolean pushed;
}
