package com.fsocial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Embeddable
public class LocalizedTemplate {

    @Column(name = "title_template", columnDefinition = "text")
    private String titleTemplate;        // "{{actor}} đã thích bài viết của bạn"

    @Column(name = "body_template", columnDefinition = "text")
    private String bodyTemplate;
}
