package com.fsocial.postservice.dto.notification;

import com.fsocial.postservice.enums.ChannelType;
import com.fsocial.postservice.enums.NotifyTo;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record NotifiDemoDTO(
        @NotBlank(message = "Title is require")
        String title,
        String description,
        String deeplink,
        ChannelType channel,
        String type,
        @NotBlank(message = "Receiver is require")
        String receiverId,
        LocalDateTime examinationTime
) {
}
