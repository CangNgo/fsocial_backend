package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.notification.DemoNotificationRequest;
import com.fsocial.postservice.dto.notification.NotificationGroupResponse;
import com.fsocial.postservice.dto.notification.NotificationResponse;
import com.fsocial.postservice.entity.Notification;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.enums.PaymentStatus;
import com.fsocial.postservice.repository.NotificationRepository;
import com.fsocial.postservice.services.DemoNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DemoNotificationServiceImpl implements DemoNotificationService {

    NotificationRepository notificationRepository;

//    @Override
//    public NotificationResponse create(DemoNotificationRequest request) {
//        Notification.NotificationBuilder builder = Notification.builder()
//                .recipientId(request.recipientId())
//                .type(request.type())
//                .title(request.title())
//                .body(request.body())
//                .isRead(false);
//
//        if (request.type() == NotificationType.APPOINTMENT) {
//            builder.examinationTime(request.examinationTime());
//        } else if (request.type() == NotificationType.PAYMENT) {
//            PaymentStatus status = request.paymentStatus() != null
//                    ? request.paymentStatus()
//                    : PaymentStatus.UNPAID;
//            builder.paymentStatus(status);
//        }
//
//        return toResponse(notificationRepository.save(builder.build()));
//    }

    @Override
    public List<NotificationGroupResponse> getByRecipient(String recipientId, NotificationType type, int page) {
        PageRequest pageable = PageRequest.of(page, 10);
        List<Notification> notifications = type != null
                ? notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(recipientId, type, pageable).getContent()
                : notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable).getContent();

        TreeMap<LocalDate, List<NotificationResponse>> grouped = new TreeMap<>(Comparator.reverseOrder());
        for (Notification n : notifications) {
            LocalDate date = n.getCreatedAt() != null
                    ? n.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                    : LocalDate.now();
            grouped.computeIfAbsent(date, k -> new java.util.ArrayList<>()).add(toResponse(n));
        }

        return grouped.entrySet().stream()
                .map(e -> new NotificationGroupResponse(e.getKey(), e.getValue()))
                .toList();
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .body(n.getBody())
                .recipientId(n.getRecipientId())
                .type(n.getType())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
