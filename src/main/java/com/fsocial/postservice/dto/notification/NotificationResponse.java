package com.fsocial.postservice.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fsocial.postservice.dto.ActorSnapshotDTO;
import com.fsocial.postservice.enums.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private String id;
    private String recipientId;
    /** Raw reference field từ entity — dùng để enrich sang {@code actor} lúc đọc, không trả ra ngoài */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String senderId;
    private NotificationType type;
    private String groupKey;
    /** Raw reference field từ entity — dùng để enrich sang {@code aggregatedActors} lúc đọc */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> aggregatedSenderIds = new ArrayList<>();
    private String title;
    private String body;
    private boolean isRead;
    private Instant createdAt;

    /** Enriched từ senderId bằng cách lookup Account, gán ở service layer */
    private ActorSnapshotDTO actor;
    /** Enriched từ aggregatedSenderIds bằng cách lookup Account, gán ở service layer */
    private List<ActorSnapshotDTO> aggregatedActors = new ArrayList<>();
}
