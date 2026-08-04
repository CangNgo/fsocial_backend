package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.MediaType;
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
@Table(name = "comment_media")
public class CommentMedia {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    Comment comment;

    @Column(name = "ord", nullable = false)
    int ord;

    @Column(name = "url", nullable = false, columnDefinition = "text")
    String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16)
    MediaType type;

    @Column(name = "width")
    Integer width;

    @Column(name = "height")
    Integer height;
}
