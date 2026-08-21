package com.fsocial.dto.faq;

import com.fsocial.enums.FaqType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FaqRequest {
    @NotBlank(message = "Tên không được để trống")
    String name;

    String description;

    @NotBlank(message = "Nội dung không được để trống")
    String content;

    @NotNull(message = "Loại không được để trống")
    FaqType type;

    String attachmentId;
}
