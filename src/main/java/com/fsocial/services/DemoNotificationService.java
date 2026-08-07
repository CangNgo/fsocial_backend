package com.fsocial.services;

import com.fsocial.dto.notification.NotificationGroupResponse;
import com.fsocial.enums.NotificationType;

import java.util.List;

public interface DemoNotificationService {
//    NotificationResponse create(DemoNotificationRequest request);
    List<NotificationGroupResponse> getByRecipient(String recipientId, NotificationType type, int page);
}
