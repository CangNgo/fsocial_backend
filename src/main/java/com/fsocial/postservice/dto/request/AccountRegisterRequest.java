package com.fsocial.postservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Data
public class AccountRegisterRequest {
    @Size(min = 6, message = "INVALID_USERNAME")
    @NotNull(message = "username is require")
    @NotBlank(message = "username is require")
    String username;

    @NotNull(message = "password is require")
    @NotBlank(message = "password is require")
    String password;

    @NotNull(message = "email is require")
    @NotBlank(message = "email is require")
    @Email(message = "INVALID_EMAIL")
    String email;

    @NotNull(message = "first name is require")
    @NotBlank(message = "first name is require")
    String firstName;

    @NotNull(message = "Last name is require")
    @NotBlank(message = "Last name is require")
    String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dob;

    int gender;
}
