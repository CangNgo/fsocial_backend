package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.enums.PaymentStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "notifications")
@CompoundIndexes({
        @CompoundIndex(name = "idx_recipient_unread_created",
                def = "{'recipientId': 1, 'isRead': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_recipient_created",
                def = "{'recipientId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_recipient_group",
                def = "{'recipientId': 1, 'groupKey': 1, 'createdAt': -1}")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    private String id;                       // ObjectId, ánh xạ sang String

    @Field("recipient_id")
    private String recipientId;

    /** Snapshot actor — denormalize để đọc không phải lookup user collection */
    private ActorSnapshot actor;

    private NotificationType type;

    /** Polymorphic reference: type + id của entity gốc */
    private EntityRef entity;

    @Field("group_key")
    private String groupKey;

    /** Khi gom nhóm: list actor gần nhất (giới hạn ~5 người) */
    @Field("aggregated_actors")
    @Builder.Default
    private List<ActorSnapshot> aggregatedActors = new ArrayList<>();

    private String title;
    private String body;

    /** Payload linh hoạt: deep link, FCM custom data, image urls... */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Field("is_read")
    private boolean isRead;

    @Field("read_at")
    private Instant readAt;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    @Field("pushed")
    private boolean pushed;
}