package com.fsocial.controller;

import com.fsocial.dto.ApiResponse;
import com.fsocial.dto.notification.DeviceTokenDTO;
import com.fsocial.dto.notification.NoticeRequest;
import com.fsocial.dto.notification.NotificationCursorResponse;
import com.fsocial.dto.notification.NotificationResponse;
import com.fsocial.postservice.dto.notification.*;
import com.fsocial.services.DemoNotificationService;
import com.fsocial.services.DeviceTokenService;
import com.fsocial.services.NotificaitonService;
import com.fsocial.services.impl.FcmServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<Void> registerToken(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody DeviceTokenDTO req
    ) {
        tokenService.registerToken(jwt.getSubject(), req.token(), req.deviceType());
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
