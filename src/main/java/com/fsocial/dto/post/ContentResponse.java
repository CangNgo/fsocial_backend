package com.fsocial.dto.post;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ContentResponse {
        String text;
        List<MediaResponse> media;
        String html;
}
