package com.fsocial.postservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OtpRequest {
    @NotBlank(message = "REQUIRED_EMAIL")
    @Email(message = "INVALID_EMAIL")
    String email;

    @NotBlank(message = "REQUIRED_OTP")
    String otp;

    @NotBlank(message = "REQUIRED_TYPE_REQUEST")
    String type;
}
