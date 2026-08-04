package com.fsocial.postservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "device_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_device_user_device",
                columnNames = {"user_id", "device_id"}))
public class DeviceToken extends AbstractEntity<String> {

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "fcm_token", nullable = false, unique = true, columnDefinition = "text")
    private String fcmToken;

    @Column(name = "device_id", length = 128, nullable = false)
    private String deviceId;

    @Column(name = "device_type", length = 32)
    private String deviceType;

    @Column(name = "device_name", length = 128)
    private String deviceName;

    @Column(name = "app_version", length = 32)
    private String appVersion;

    @Column(name = "os_version", length = 32)
    private String osVersion;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "last_use_at")
    private LocalDateTime lastUsedAt;
}
