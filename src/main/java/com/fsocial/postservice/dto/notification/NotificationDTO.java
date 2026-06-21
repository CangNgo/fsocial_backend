package com.fsocial.postservice.dto.notification;

import java.util.Map;

public record NotificationDTO(
        String token,
        String title,
        String body,
        Map<String, String> data 
) {}