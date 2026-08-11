package com.fsocial.dto.message;

import com.fsocial.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SendMessageRequest {
    String conversationId;
    String content;
    @Builder.Default
    MessageType messageType = MessageType.TEXT;
    String replyToId;
}
