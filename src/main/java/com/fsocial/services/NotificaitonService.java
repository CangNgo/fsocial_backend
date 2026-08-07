package com.fsocial.services;

import com.fsocial.dto.notification.NoticeRequest;
import com.fsocial.dto.notification.NotificationDTO;
import com.fsocial.dto.notification.NotificationCursorResponse;
import com.fsocial.dto.notification.NotificationResponse;

public interface NotificaitonService {
    NotificationResponse createNotification(NoticeRequest notificationRequest);
    void notifcationCreateConsumer (NotificationDTO dto) ;
    NotificationCursorResponse getNotifications(String userId, String cursor);
    long getCountNotificationByRecipientId(String recipient);
    NotificationResponse readNotification(String notificationId);
}
