package com.fsocial.postservice.dto.post;

import com.fsocial.postservice.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Kết quả upload 1 file media. Thay cho entity {@code MediaItem} cũ của Mongo:
 * bản lưu trữ giờ là bảng {@code attachments}, còn đây thuần là DTO vận chuyển giữa
 * Cloudinary → service → API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaItemDTO {
    String attachmentId;
    String url;
    String type; // "image" | "video"
    MediaType mediaType;
    Integer width;
    Integer height;
}
