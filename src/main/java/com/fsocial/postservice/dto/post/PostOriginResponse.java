package com.fsocial.postservice.dto.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostOriginResponse(
        String id,
        String userId,
        ContentResponse content,
        String displayName,
        String avatar,
        LocalDateTime createDatetime,
        List<String>tags
) {
}
