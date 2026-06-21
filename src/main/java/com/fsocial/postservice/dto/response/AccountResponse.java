package com.fsocial.postservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Data
public class AccountResponse {
    String id;
    String username;
    boolean isKOL;
    String role;
    String displayName;
    String avatar;
    String background;
    String bio;
}
