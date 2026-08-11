package com.fsocial.dto.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AttachmentsRequest {

    @Schema(description = "Last id", defaultValue = "43e76a29-4153-42aa-b692-1bc65fab7f8d")
    String lastItemId;

    @Schema(description = "Last created", defaultValue = "2026-08-05 19:26:28.763517")
    String createdAt;

    @Schema(description = "Media type", defaultValue = "image")
    String resourceType;
}
