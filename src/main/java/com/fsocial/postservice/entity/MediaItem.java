package com.fsocial.postservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaItem {
    String url;
    String type; // "image" | "video"
    Integer width;
    Integer height;
}
