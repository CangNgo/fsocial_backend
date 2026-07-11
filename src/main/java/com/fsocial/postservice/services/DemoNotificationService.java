package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.notification.DemoNotificationRequest;
import com.fsocial.postservice.dto.notification.NotificationGroupResponse;
import com.fsocial.postservice.dto.notification.NotificationResponse;
import com.fsocial.postservice.enums.NotificationType;

import java.util.List;

public interface DemoNotificationService {
//    NotificationResponse create(DemoNotificationRequest request);
    List<NotificationGroupResponse> getByRecipient(String recipientId, NotificationType type, int page);
}
