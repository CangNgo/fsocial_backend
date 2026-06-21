package com.fsocial.postservice.dto.notification;

import java.time.LocalDate;
import java.util.List;

public record NotificationGroupResponse(
        LocalDate date,
        List<NotificationResponse> notifications
) {
}
