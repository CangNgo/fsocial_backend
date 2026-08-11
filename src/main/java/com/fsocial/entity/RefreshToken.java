package com.fsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36)
    String id;

    @Column(name = "token", nullable = false, unique = true, columnDefinition = "text")
    String token;

    @Column(name = "account_id", length = 36, nullable = false)
    String accountId;

    @Column(name = "expiry_date", nullable = false)
    Instant expiryDate;

    @Column(name = "user_agent", columnDefinition = "text")
    String userAgent;

    @Column(name = "ip_address", length = 64)
    String ipAddress;
}
