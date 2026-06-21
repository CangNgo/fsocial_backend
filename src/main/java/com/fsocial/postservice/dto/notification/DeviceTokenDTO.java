package com.fsocial.postservice.dto.notification;

public record DeviceTokenDTO(
        String userId,
        String token,
        String deviceType
) {}
