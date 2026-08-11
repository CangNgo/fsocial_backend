package com.fsocial.dto.Attachments;

import com.fsocial.enums.AttachmentType;

import java.time.LocalDateTime;

public interface AttachementProfileDTO {
    String getId();
    String getPublicId();
    String getUrl();
    String getResourceType();
    String getFileType();
    String getSize();
    String getOwnerId();
    String getPostId();
    Integer getOrd();
    Integer getWidth();
    Integer getHeight();
    AttachmentType getType();
    LocalDateTime getCreatedAt();
}
