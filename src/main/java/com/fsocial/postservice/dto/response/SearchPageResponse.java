package com.fsocial.postservice.dto.response;

import java.util.List;

public record SearchPageResponse<T>(
        List<T> items,
        int page,
        int size,
        boolean hasMore
) {
}
