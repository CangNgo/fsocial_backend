package com.fsocial.postservice.scheduler;

import com.fsocial.postservice.entity.Notification;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.repository.NotificationRepository;
import com.fsocial.postservice.services.DeviceTokenService;
import com.fsocial.postservice.services.impl.FcmServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentReminderScheduler {

    NotificationRepository notificationRepository;
    DeviceTokenService deviceTokenService;
    FcmServiceImpl fcmService;

    @Scheduled(fixedRate = 60_000)
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursLater = now.plusHours(2);

        List<Notification> upcoming = notificationRepository
                .findByTypeAndPushedFalseAndExaminationTimeBetween(
                        NotificationType.APPOINTMENT, now, twoHoursLater);

        if (upcoming.isEmpty()) return;

        log.info("Found {} appointment(s) to remind", upcoming.size());

        for (Notification notification : upcoming) {
            List<String> tokens = deviceTokenService.getTokenByUserId(notification.getRecipientId());
            if (tokens.isEmpty()) {
                log.warn("No device tokens for recipientId={}", notification.getRecipientId());
                markPushed(notification);
                continue;
            }

            try {
                fcmService.sendToMultipleTokens(
                        tokens,
                        notification.getTitle(),
                        buildBody(notification),
                        Map.of("notificationId", notification.getId(),
                               "type", NotificationType.APPOINTMENT.name())
                );
                markPushed(notification);
            } catch (Exception e) {
                log.error("Failed to push reminder for notificationId={}", notification.getId(), e);
            }
        }
    }

    private String buildBody(Notification notification) {
        if (notification.getBody() != null && !notification.getBody().isBlank()) {
            return notification.getBody();
        }
        return "Bạn có lịch hẹn vào lúc " + notification.getExaminationTime();
    }

    private void markPushed(Notification notification) {
        notification.setPushed(true);
        notificationRepository.save(notification);
    }
}
