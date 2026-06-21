package com.fsocial.postservice.entity;


import com.fsocial.postservice.enums.NotificationType;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Document(collection = "notification_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class NotificationTemplate extends AbstractEntity<String> {
    @Indexed(unique = true)
    private NotificationType type;

    /** Map locale ("vi", "en", "ja"...) → template tương ứng */
    @Builder.Default
    private Map<String, LocalizedTemplate> translations = new HashMap<>();

    @Field("default_data")
    @Builder.Default
    private Map<String, Object> defaultData = new HashMap<>();

    @Field("is_active")
    @Builder.Default
    private boolean isActive = true;

}

