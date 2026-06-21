package com.fsocial.postservice.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Stores co-occurrence counts between tag pairs for Exploration feed strategy.
 * Used to find related tags when building the Explore (20%) portion of the feed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "tag_cooccurrence")
@CompoundIndexes({
    @CompoundIndex(name = "idx_tagA_tagB", def = "{'tag_a': 1, 'tag_b': 1}", unique = true),
    @CompoundIndex(name = "idx_tagA_count", def = "{'tag_a': 1, 'count': -1}")
})
public class TagCooccurrence {

    @Id
    String id;

    @Field("tag_a")
    String tagA;

    @Field("tag_b")
    String tagB;

    @Field("count")
    int count;
}
