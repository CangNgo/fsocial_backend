package com.fsocial.mapper;

import com.fsocial.dto.message.ConversationDTO;
import com.fsocial.dto.message.MessageDTO;
import com.fsocial.entity.Conversation;
import com.fsocial.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Members / lastMessage không map ở đây — service tự build vì cần batch-resolve
 * qua AccountRepository/MessageRepository (tránh N+1), giống PostMapper.
 */
@Mapper(componentModel = "spring")
public interface ChatMapper {

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "replyToId", source = "replyTo.id")
//    @Mapping(target ="actorSnapshotDTO.userId", source = "message.")
    MessageDTO toMessageDTO(Message message);

    ConversationDTO toConversationDTO(Conversation conversation);
}
