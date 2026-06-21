package com.fsocial.postservice.entity;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Document(collection = "notification_preferences")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class NotificationPreference extends AbstractEntity<String>{
    @Indexed(unique = true)
    @Field("user_id")
    private String userId;

    /** Key là tên NotificationType (LIKE, COMMENT...), value là setting per-channel */
    @Builder.Default
    private Map<String, ChannelSettings> settings = new HashMap<>();

    @Field("quiet_hours_start")
    private String quietHoursStart;     // "22:00"

    @Field("quiet_hours_end")
    private String quietHoursEnd;       // "07:00"

    private String timezone;            // "Asia/Ho_Chi_Minh"
}