package com.fsocial.dto.Attachments;

import com.fsocial.enums.AttachmentType;
import com.fsocial.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttachmentDTO {
    String id;
    String publicId;
    String url;
    String resourceType;
    String fileType;
    String size;
    String ownerId;
    String postId;
    Integer ord;
    Integer width;
    Integer height;
    AttachmentType type;
    MediaType mediaType;
    LocalDateTime createdAt;

    public AttachmentDTO(String id, String publicId, String url, String resourceType, String fileType,
                         String size, String ownerId, LocalDateTime createdAt) {
        this.id = id;
        this.publicId = publicId;
        this.url = url;
        this.resourceType = resourceType;
        this.fileType = fileType;
        this.size = size;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }
}
