package com.fsocial.postservice.dto.notification;

import com.fsocial.postservice.entity.Notification;

import java.util.List;

public record NotificationCursorResponse(
        List<NotificationResponse> items,
        String nextCursor,
        boolean hasMore
) {
}
