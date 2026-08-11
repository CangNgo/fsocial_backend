package com.fsocial.dto.message;

import com.fsocial.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CreateConversationRequest {
    List<String> memberIds;
    @Builder.Default
    ConversationType type = ConversationType.DIRECT;
    String name;
}
