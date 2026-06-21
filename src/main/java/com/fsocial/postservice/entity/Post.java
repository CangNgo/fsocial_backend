package com.fsocial.postservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "post")
@SuperBuilder
@CompoundIndexes({
    @CompoundIndex(name = "idx_tags_score", def = "{'tags': 1, 'global_score': -1}"),
    @CompoundIndex(name = "idx_author_created", def = "{'owner.user_id': 1, 'created_datetime': -1}")
})
public class Post extends AbstractEntity<String> {
    @Field("content")
    Content content;
    @Field("likes")
    List<String> likes = new ArrayList<>();
    @Field("created_datetime")
    LocalDateTime createDatetime = LocalDateTime.now();
    //share
    @Field("origin_post")
    String originPostId;
    @Field("is_share")
    @Builder.Default
    Boolean isShare = false;
    @Field("status")
    @Builder.Default
    Boolean status = true;
    //owner
    @Field("owner")
    Owner owner;
    // Feed recommendation fields (BRD)
    @Field("tags")
    @Builder.Default
    List<String> tags = new ArrayList<>();
    @Field("global_score")
    @Builder.Default
    double globalScore = 0.0;
    @Field("share_count")
    @Builder.Default
    int shareCount = 0;
}
