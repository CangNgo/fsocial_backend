package com.fsocial.postservice.dto.notification;

import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DemoNotificationRequest(
        @NotBlank(message = "Title is required")
        String title,
        String body,
        @NotBlank(message = "RecipientId is required")
        String recipientId,
        @NotNull(message = "Type is required")
        NotificationType type,
        String deeplink,
        LocalDateTime examinationTime,
        PaymentStatus paymentStatus
) {
}
