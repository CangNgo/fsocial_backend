package com.fsocial.postservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "refresh_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RefreshToken {
    @Id
    String id = UUID.randomUUID().toString();

    @Field("token")
    String token;

    @Field("username")
    String username;

    @Field("expiry_date")
    Instant expiryDate;

    @Field("user_agent")
    String userAgent;

    @Field("ip_address")
    String ipAddress;
}
