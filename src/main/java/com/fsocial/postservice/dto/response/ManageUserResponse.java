package com.fsocial.postservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ManageUserResponse {
    String id;
    String username;
    String displayName;
    String email;
    LocalDateTime createdAt;
    LocalDateTime updatedAt; // ponytail: proxy for "last active" — no real activity tracking exists yet
    boolean status;
}
