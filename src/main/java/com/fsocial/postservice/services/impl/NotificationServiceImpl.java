package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.notification.NoticeRequest;
import com.fsocial.postservice.dto.notification.NotificationCursorResponse;
import com.fsocial.postservice.dto.notification.NotificationDTO;
import com.fsocial.postservice.dto.notification.NotificationResponse;
import com.fsocial.postservice.entity.Notification;
import com.fsocial.postservice.enums.NotificationConst;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.repository.NotificationRepository;
import com.fsocial.postservice.services.NotificaitonService;
import com.fsocial.postservice.util.DateUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificaitonService {

    RestTemplate restTemplate;
    NotificationRepository notificationRepository;

    @NonFinal
    @Value("${app.services.notification}")
    String notificationServiceUrl;

    @Override
    public NotificationResponse createNotification(NoticeRequest notificationRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NoticeRequest> entity = new HttpEntity<>(notificationRequest, headers);

        ResponseEntity<ApiResponse<NotificationResponse>> response = restTemplate.exchange(
                notificationServiceUrl + "/notification/notice",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        ApiResponse<NotificationResponse> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    @Override
    public void notifcationCreateConsumer(NotificationDTO dto) {
        NotificationConst notiConst = NotificationConst.from(dto.type());
        Object bodyArg = (dto.type() == NotificationType.LOGIN)
                ? DateUtils.getNow()
                : dto.actor().getDisplayName();

        Notification entity = Notification.builder()
                .title(notiConst.getTitle())
                .body(String.format(notiConst.getBody(), bodyArg))
                .recipientId(dto.recipientId())
                .actor(dto.actor())
                .type(dto.type())
                .build();
        notificationRepository.save(entity);
    }

    static final int PAGE_SIZE = 10;

    @Override
    public NotificationCursorResponse getNotifications(String userId, String cursor) {
        // Sort theo _id DESC — ObjectId chứa timestamp nên tương đương created_at DESC,
        // lại unique nên cursor không bị trùng. Fetch PAGE_SIZE + 1 để biết còn trang sau không.
        var pageable = PageRequest.of(0, PAGE_SIZE + 1);
        List<NotificationResponse> found = (cursor == null || cursor.isBlank())
                ? notificationRepository.findByRecipientIdOrderByIdDesc(userId, pageable)
                : notificationRepository.findByRecipientIdAndIdLessThanOrderByIdDesc(
                        userId, cursor, pageable);

        boolean hasMore = found.size() > PAGE_SIZE;
        List<NotificationResponse> items = hasMore ? found.subList(0, PAGE_SIZE) : found;
        String nextCursor = hasMore ? items.getLast().getId() : null;

        return new NotificationCursorResponse(items, nextCursor, hasMore);
    }

    @Override
    public long getCountNotificationByRecipientId(String recipient) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipient);
    }
}
