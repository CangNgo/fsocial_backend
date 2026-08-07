package com.fsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gộp cả comment gốc và reply vào một bảng: parent_id NULL = comment gốc,
 * khác NULL = reply. Thay cho mảng embedded {@code replies[]} của MongoDB.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "comment")
@SuperBuilder
public class Comment extends AbstractEntity<String> {

    @Column(name = "post_id", length = 36, nullable = false)
    String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createDatetime ASC")
    @Builder.Default
    List<Comment> replies = new ArrayList<>();

    @Column(name = "user_id", length = 36, nullable = false)
    String userId;

    @Column(name = "text", columnDefinition = "text")
    String text;

    @Column(name = "html", columnDefinition = "text")
    String html;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ord ASC")
    @Builder.Default
    List<CommentMedia> media = new ArrayList<>();

    @Column(name = "create_datetime", nullable = false)
    @Builder.Default
    LocalDateTime createDatetime = LocalDateTime.now();

    /** Suy ra từ parent thay vì lưu cột riêng như bản Mongo. */
    @Transient
    public boolean isReply() {
        return parent != null;
    }

    public void addMedia(CommentMedia item) {
        item.setComment(this);
        item.setOrd(media.size());
        media.add(item);
    }
}
