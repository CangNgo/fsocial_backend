package com.fsocial.postservice.dto.post;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostDTORequest {
    String userId;
    String text;
    String html;
    MultipartFile[] media;
    List<String> tags;
}
