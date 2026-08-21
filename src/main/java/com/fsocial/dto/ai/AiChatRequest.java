package com.fsocial.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AiChatRequest {
    // WS la fire-and-forget: khong co requestId thi client khong the ghep cau tra loi
    // ve dung cau hoi khi nguoi dung hoi lien tiep.
    String requestId;
    String prompt;
}
