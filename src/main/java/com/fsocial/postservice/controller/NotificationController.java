package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.Response;
import com.fsocial.postservice.dto.notification.*;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.services.DemoNotificationService;
import com.fsocial.postservice.services.DeviceTokenService;
import com.fsocial.postservice.services.NotificaitonService;
import com.fsocial.postservice.services.impl.FcmServiceImpl;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/notification")
public class NotificationController {

    NotificaitonService notificationService;
    FcmServiceImpl fcmService;
    DeviceTokenService tokenService;
    DemoNotificationService demoNotificationService;

    @PostMapping("/register-token")
    public ResponseEntity<Void> registerToken(@RequestBody DeviceTokenDTO req) {
        tokenService.registerToken(req.userId(), req.token(), req.deviceType());
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<String> sendToUser(
            @PathVariable String userId,
            @RequestBody NotificationDTO req) throws FirebaseMessagingException {
        List<String> tokens = tokenService.getTokenByUserId(userId);
        if (tokens.isEmpty()) return ResponseEntity.notFound().build();

        BatchResponse res = fcmService.sendToMultipleTokens(
                tokens, req.title(), req.body(), req.data());
        return ResponseEntity.ok("Sent: " + res.getSuccessCount());
    }

    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(@RequestBody NoticeRequest notificationRequest) {

        return ApiResponse.<NotificationResponse>builder()
                .statusCode(200)
                .data(notificationService.createNotification(notificationRequest))
                .message("Create notification success")
                .build();
    }

}
