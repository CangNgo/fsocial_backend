package com.fsocial.postservice.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class LocalizedTemplate {
    @Field("title_template")
    private String titleTemplate;        // "{{actor}} đã thích bài viết của bạn"

    @Field("body_template")
    private String bodyTemplate;
}