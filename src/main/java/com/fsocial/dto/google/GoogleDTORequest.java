package com.fsocial.dto.google;

import jakarta.validation.constraints.NotBlank;

public record GoogleDTORequest(
        @NotBlank(message = "code is required")
        String code
) {
}
