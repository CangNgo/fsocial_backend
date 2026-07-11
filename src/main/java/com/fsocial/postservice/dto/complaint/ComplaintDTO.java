package com.fsocial.postservice.dto.complaint;

import com.fsocial.postservice.enums.ComplaintType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComplaintDTO {
    @NotBlank(message = "postId không được để trống")
    String postId;
    @NotNull(message = "complaintType không được để trống")
    ComplaintType complaintType;
    @NotBlank(message = "termOfServiceId không được để trống")
    String termOfServiceId;
}
