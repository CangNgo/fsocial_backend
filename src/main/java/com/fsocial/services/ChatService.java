package com.fsocial.services;

import com.fsocial.dto.message.*;

import java.util.List;

public interface ChatService {

    List<ConversationDTO> getConversationsForUser(String userId);

    ConversationDTO createConversation(String userId, CreateConversationRequest request);

    List<MessageDTO> getMessages(String conversationId, String userId, String cursor);

    MessageDTO sendMessage(String userId, SendMessageRequest request);

    List<String> getMemberIds(String conversationId);

    void markReadConversation(MarkReadDTO request, String userId);
}
