package com.fsocial.postservice.dto.post;

import com.fsocial.postservice.entity.MediaItem;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

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
