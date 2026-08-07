package com.fsocial.dto.notification;

import java.util.List;

public record NotificationCursorResponse(
        List<NotificationResponse> items,
        String nextCursor,
        boolean hasMore
) {
}
