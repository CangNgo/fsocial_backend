package com.fsocial.postservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Data
public class AccountLoginRequest {
    @NotBlank(message = "User name is require")
    @Size(min = 6, message = "User name must have least 6 characters")
    String username;

    @NotBlank(message = "REQUIRED_PASSWORD")
    @Size(min = 6, message = "Password must have least 6 characters")
    String password;
}
