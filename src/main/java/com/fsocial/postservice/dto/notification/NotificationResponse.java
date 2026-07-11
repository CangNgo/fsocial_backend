package com.fsocial.postservice.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fsocial.postservice.entity.ActorSnapshot;
import com.fsocial.postservice.entity.EntityRef;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private String id;
    private String recipientId;
    private ActorSnapshot actor;
    private NotificationType type;
    private String groupKey;
    private List<ActorSnapshot> aggregatedActors = new ArrayList<>();
    private String title;
    private String body;
    private boolean isRead;
    private Instant createdAt;
}