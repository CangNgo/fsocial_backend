package com.fsocial.postservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RefreshTokenRequest {
    @NotNull(message = "token is require")
    @NotBlank(message = "token is require")
    String refreshToken;
}
