package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.notification.NoticeRequest;
import com.fsocial.postservice.dto.notification.NotificationResponse;
import com.fsocial.postservice.services.NotificaitonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificaitonService {

    RestTemplate restTemplate;

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
}
