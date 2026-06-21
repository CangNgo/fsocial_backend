package com.fsocial.postservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Data
public class ResetPasswordRequest {
    @NotNull(message = "email is require")
    @NotBlank(message = "email is require")
    @Email(message = "INVALID_EMAIL")
    String email;

    @NotNull(message = "password is require")
    @NotBlank(message = "password is require")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String newPassword;
}
