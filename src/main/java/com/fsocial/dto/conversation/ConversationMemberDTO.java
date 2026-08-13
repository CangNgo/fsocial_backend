package com.fsocial.dto.conversation;

import com.fsocial.dto.ActorSnapshotDTO;
import com.fsocial.dto.message.MessageDTO;
import com.fsocial.entity.Conversation;
import com.fsocial.enums.ConversationType;
import com.fsocial.enums.MemberRole;
import com.fsocial.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationMemberDTO {
    String id;
    ConversationType type;
    String name;
    String conversationAvatar;
    String userId;
    String displayName;
    String avatar;
    MessageDTO lastMessage;
    double unreadCount;

    String messageId;
    String senderId;
    String content;
    MessageType messageType;
    String replyToId;
    LocalDateTime createdAt;
}
