package com.fsocial.postservice.dto.google;

import jakarta.validation.constraints.NotBlank;

public record GoogleDTORequest(
        @NotBlank(message = "code is required")
        String code
) {
}
