package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.services.DeviceTokenService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenService tokenService;

    /**
     * Gửi đến 1 thiết bị.
     * Tự động xóa token nếu nhận UNREGISTERED/INVALID_ARGUMENT.
     */
    public String sendToToken(String token, String title, String body,
                              Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data == null ? Map.of() : data)
                .setAndroidConfig(androidConfig())
                .setApnsConfig(apnsConfig())
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("FCM sent: {}", response);
            return response;
        } catch (FirebaseMessagingException e) {
            handleSendError(e, token);
            throw e;
        }
    }

    /**
     * Gửi đến nhiều token cùng lúc (max 500).
     */
    public BatchResponse sendToMultipleTokens(List<String> tokens, String title,
                                              String body, Map<String, String> data)
            throws FirebaseMessagingException {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title).setBody(body).build())
                .putAllData(data == null ? Map.of() : data)
                .setAndroidConfig(androidConfig())
                .setApnsConfig(apnsConfig())
                .build();

        BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
        log.info("Multicast: {} success, {} failed",
                response.getSuccessCount(), response.getFailureCount());

        // Xóa các token bị lỗi
        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse sr = response.getResponses().get(i);
            if (!sr.isSuccessful() && isInvalidToken(sr.getException())) {
                tokenService.removeInvalidToken(tokens.get(i));
            }
        }
        return response;
    }

    private void handleSendError(FirebaseMessagingException e, String token) {
        if (isInvalidToken(e)) {
            log.warn("Removing invalid token: {}", token);
            tokenService.removeInvalidToken(token);
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException e) {
        if (e == null) return false;
        MessagingErrorCode code = e.getMessagingErrorCode();
        return code == MessagingErrorCode.UNREGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT;
    }

    private AndroidConfig androidConfig() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setChannelId("default")     // ← phải khớp client
                        .setSound("default")
                        .build())
                .build();
    }

    private ApnsConfig apnsConfig() {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .setContentAvailable(true)
                        .build())
                .build();
    }
}