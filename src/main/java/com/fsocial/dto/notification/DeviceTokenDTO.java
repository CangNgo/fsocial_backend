package com.fsocial.dto.notification;

public record DeviceTokenDTO(
        String userId,
        String token,
        String deviceType
) {}
