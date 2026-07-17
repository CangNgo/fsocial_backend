package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.ComplaintType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "complaint")
@Builder
public class Complaint extends  AbstractEntity<String>{
    String targetId;
    ComplaintType complaintType;
    boolean isRead;
    @Builder.Default
    List<ComplaintDetail> details = new ArrayList<>();
}
