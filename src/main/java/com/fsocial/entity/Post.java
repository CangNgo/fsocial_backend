package com.fsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "post")
@SuperBuilder
public class Post extends AbstractEntity<String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    Account owner;

    @Column(name = "text", columnDefinition = "text")
    String text;

    @Column(name = "html", columnDefinition = "text")
    String html;

    @OneToMany(mappedBy = "post")
    @OrderBy("ord ASC")
    @Builder.Default
    List<Attachments> media = new ArrayList<>();

    @Column(name = "create_datetime", nullable = false)
    @Builder.Default
    LocalDateTime createDatetime = LocalDateTime.now();

    // share
    @Column(name = "origin_post_id", length = 36)
    String originPostId;

    @Column(name = "is_share", nullable = false)
    @Builder.Default
    Boolean isShare = false;

    @Column(name = "status", nullable = false)
    @Builder.Default
    Boolean status = true;

    // Feed recommendation fields (BRD)
    @Column(name = "global_score", nullable = false)
    @Builder.Default
    double globalScore = 0.0;

    @Column(name = "raw_engagement", nullable = false)
    @Builder.Default
    double rawEngagement = 0.0;

    @Column(name = "share_count", nullable = false)
    @Builder.Default
    int shareCount = 0;

    // likes vẫn qua PostLikeRepository (chỉ cần count/exists, không load mảng).
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    @Builder.Default
    List<String> tags = new ArrayList<>();

    public void addMedia(Attachments item) {
        item.setPost(this);
        item.setOrd(media.size());
        media.add(item);
    }
}
