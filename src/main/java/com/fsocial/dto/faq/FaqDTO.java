package com.fsocial.dto.faq;

import com.fsocial.enums.FaqType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FaqDTO {
    String id;
    String name;
    String description;
    String content;
    FaqType type;
    String attachmentId;
    long viewCount;
    long ratingCount;
    double averageRating;
}
