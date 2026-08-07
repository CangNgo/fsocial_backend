package com.fsocial.dto.notification;

import com.fsocial.enums.NotificationType;

public record NotificationDTO(
         String recipientId,

         String senderId,

         NotificationType type
){}
