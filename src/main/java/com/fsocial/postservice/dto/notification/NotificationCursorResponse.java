package com.fsocial.postservice.dto.notification;

import java.util.List;

public record NotificationCursorResponse(
        List<NotificationResponse> items,
        String nextCursor,
        boolean hasMore
) {
}
