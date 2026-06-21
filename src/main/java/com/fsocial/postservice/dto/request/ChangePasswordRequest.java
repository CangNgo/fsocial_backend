package com.fsocial.postservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChangePasswordRequest {
    @NotNull(message = "password is require")
    @NotBlank(message = "password is require")
    String oldPassword;

    @NotNull(message = "password is require")
    @NotBlank(message = "password is require")
    @Size(min = 8, message = "INVALID_PASSWORD")
    String newPassword;
}
