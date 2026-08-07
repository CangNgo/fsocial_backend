package com.fsocial.entity;

import com.fsocial.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Entity
@Table(name = "account")
public class Account extends AbstractEntity<String> {

    @Column(name = "username", length = 64, nullable = false, unique = true)
    String username;

    @Column(name = "password")
    String password;

    @Column(name = "first_name", length = 128)
    String firstName;

    @Column(name = "last_name", length = 128)
    String lastName;

    @Column(name = "display_name")
    String displayName;

    @Column(name = "dob")
    LocalDate dob;

    @Column(name = "gender", nullable = false)
    int gender;

    @Column(name = "avatar", columnDefinition = "text")
    String avatar;

    @Column(name = "background", columnDefinition = "text")
    String background;

    @Column(name = "bio", columnDefinition = "text")
    String bio;

    @Column(name = "address", columnDefinition = "text")
    String address;

    @Column(name = "is_kol", nullable = false)
    @Builder.Default
    boolean isKOL = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    Role role;

    /** Token giữ FK account_id — bên này chỉ đọc. */
    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    Token token;

    @Column(name = "email")
    String email;

    @Column(name = "status", nullable = false)
    @Builder.Default
    boolean status = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 16, nullable = false)
    @Builder.Default
    AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "google_id", length = 64)
    String googleId;

    // follower/following (Set<String>) -> bảng `follow`, truy vấn qua FollowRepository

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    Boolean isPublic = true;
}
