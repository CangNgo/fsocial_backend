package com.fsocial.dto.notification;

import com.fsocial.enums.ChannelType;
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
