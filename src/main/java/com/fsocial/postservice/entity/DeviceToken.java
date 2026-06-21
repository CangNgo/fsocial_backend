package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.DeviceType;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "device_tokens")
@CompoundIndexes({
        @CompoundIndex(name = "uk_user_device",
                def = "{'userId': 1, 'deviceId': 1}",
                unique = true),
        @CompoundIndex(name = "idx_user_active",
                def = "{'userId': 1, 'isActive': 1}")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DeviceToken extends AbstractEntity<String> {

    @Field("user_id")
    private String userId;

    @Indexed(unique = true)
    @Field("fcm_token")
    private String fcmToken;

    @Field("device_id")
    private String deviceId;

    @Field("device_type")
    private String deviceType;

    @Field("device_name")
    private String deviceName;

    @Field("app_version")
    private String appVersion;

    @Field("os_version")
    private String osVersion;

    @Field("is_active")
    @Builder.Default
    private boolean isActive = true;

    @Field("last_use_at")
    private LocalDateTime lastUsedAt;
}
