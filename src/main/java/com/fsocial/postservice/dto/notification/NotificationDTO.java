package com.fsocial.postservice.dto.notification;

import com.fsocial.postservice.enums.NotificationType;

public record NotificationDTO(
         String recipientId,

         String senderId,

         NotificationType type
){}
