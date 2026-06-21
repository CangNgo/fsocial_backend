package com.fsocial.postservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
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
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "user_interests")
@CompoundIndexes({
    @CompoundIndex(name = "idx_interests_tag", def = "{'interests.tag': 1}")
})
public class UserInterests {

    @Id
    String id;

    @Field("user_id")
    @Indexed(unique = true)
    String userId;

    @Field("interests")
    @Builder.Default
    List<InterestItem> interests = new ArrayList<>();

    @Field("updated_at")
    LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterestItem {
        @Field("tag")
        String tag;
        @Field("weight")
        double weight;
        @Field("updated_at")
        LocalDateTime updatedAt;
    }
}
