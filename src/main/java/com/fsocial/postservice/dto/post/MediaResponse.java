package com.fsocial.postservice.dto.post;

import com.fsocial.postservice.enums.MediaLayoutType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MediaResponse {
    String url;
    String type; // "image" | "video"
    Integer width;
    Integer height;
    double ratio;
    MediaLayoutType mediaType;
}
