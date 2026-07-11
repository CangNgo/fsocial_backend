package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.ComplaintType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "complaint")
@Builder
public class Complaint extends  AbstractEntity<String>{
    String postId;
    String userId;
    ComplaintType complaintType;
    String termOfServiceId;
    @Field("created_datetime")
    LocalDateTime createDatetime = LocalDateTime.now();
    boolean isRead;
}
