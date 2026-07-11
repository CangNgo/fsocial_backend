package com.fsocial.postservice.dto.notification;

import com.fsocial.postservice.entity.ActorSnapshot;
import com.fsocial.postservice.enums.NotificationType;

public record NotificationDTO(
         String recipientId,

         ActorSnapshot actor,

         NotificationType type
){}