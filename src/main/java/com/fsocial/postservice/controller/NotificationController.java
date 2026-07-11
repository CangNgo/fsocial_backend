package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ApiResponse<Void> registerToken(@RequestBody DeviceTokenDTO req) {
        tokenService.registerToken(req.userId(), req.token(), req.deviceType());
        return ApiResponse.<Void>builder()
                .message("Register token success")
                .build();
    }

//    @PostMapping("/send/{userId}")
//    public ResponseEntity<ApiResponse<String>> sendToUser(
//            @PathVariable String userId,
//            @RequestBody NotificationDTO req) throws FirebaseMessagingException {
//        List<String> tokens = tokenService.getTokenByUserId(userId);
//        if (tokens.isEmpty()) {
//            return ResponseEntity.status(404).body(ApiResponse.<String>builder()
//                    .statusCode(404)
//                    .message("Device token not found")
//                    .build());
//        }
//
//        BatchResponse res = fcmService.sendToMultipleTokens(
//                tokens, req.title(), req.body(), req.data());
//        return ResponseEntity.ok(ApiResponse.<String>builder()
//                .data("Sent: " + res.getSuccessCount())
//                .message("Send notification success")
//                .build());
//    }

    @GetMapping
    public ApiResponse<NotificationCursorResponse> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String cursor) {
        return ApiResponse.<NotificationCursorResponse>builder()
                .data(notificationService.getNotifications(jwt.getSubject(), cursor))
                .message("Get notifications success")
                .build();
    }

    @GetMapping("/un-read")
    public ApiResponse<Long> getNotificationsUnRead(
            @AuthenticationPrincipal Jwt jwt
    ){
        return ApiResponse.<Long>builder()
                .data(notificationService.getCountNotificationByRecipientId(jwt.getSubject()))
                .message("Get notification un read success")
                .build();
    }

    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(@RequestBody NoticeRequest notificationRequest) {
        return ApiResponse.<NotificationResponse>builder()
                .data(notificationService.createNotification(notificationRequest))
                .message("Create notification success")
                .build();
    }
}
