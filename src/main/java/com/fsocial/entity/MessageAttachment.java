package com.fsocial.entity;

import com.fsocial.enums.MessageAttachmentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "message_attachments")
public class MessageAttachment extends AbstractEntity<String>{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    Message message;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 16, nullable = false)
    MessageAttachmentType attachmentType;

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    String url;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    String thumbnailUrl;

    @Column(name = "file_name", length = 255)
    String fileName;

    @Column(name = "file_size")
    Long fileSize;

    @Column(name = "mime_type", length = 100)
    String mimeType;

    @Column(name = "width")
    Integer width;

    @Column(name = "height")
    Integer height;

    @Column(name = "duration_seconds")
    Integer durationSeconds;
}
