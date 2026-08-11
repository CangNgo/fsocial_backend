package com.fsocial.entity;

import com.fsocial.enums.AttachmentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "attachments")
@SuperBuilder
public class Attachments extends AbstractEntity<String> {

    @Column(name = "public_id")
    String publicId;

    @Column(name = "resource_type", length = 64)
    String resourceType;

    @Column(name = "file_type", length = 64)
    String fileType;

    @Column(name = "size", length = 64)
    String size;

    @Column(name = "url", columnDefinition = "text")
    String url;

    @Column(name = "owner_id", length = 36)
    String ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    Post post;

    @Column(name = "ord")
    Integer ord;

    @Column(name = "width")
    Integer width;

    @Column(name = "height")
    Integer height;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16)
    AttachmentType type;
}
