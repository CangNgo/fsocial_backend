package com.fsocial.postservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Data
public class AccountResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    String email;
    String dob;
    int gender;
    String address;
    boolean isKOL;
    String role;
    String displayName;
    String avatar;
    String background;
    String bio;
    Set<String> follower = new HashSet<>();
    Set<String> following = new HashSet<>();
}
