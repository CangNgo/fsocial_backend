package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.EntityType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityRef {
    private EntityType type;
    private String id;
    private String preview;       // VD: 50 ký tự đầu của post body
    private String thumbnailUrl;
}