package com.fsocial.services.impl;

import com.fsocial.dto.message.ConversationDTO;
import com.fsocial.dto.message.CreateConversationRequest;
import com.fsocial.dto.message.MessageDTO;
import com.fsocial.dto.message.SendMessageRequest;
import com.fsocial.entity.Conversation;
import com.fsocial.entity.ConversationMember;
import com.fsocial.entity.Message;
import com.fsocial.enums.ConversationType;
import com.fsocial.exception.AppException;
import com.fsocial.exception.StatusCode;
import com.fsocial.mapper.ChatMapper;
import com.fsocial.repository.AccountRepository;
import com.fsocial.repository.ConversationMemberRepository;
import com.fsocial.repository.ConversationRepository;
import com.fsocial.repository.MessageRepository;
import com.fsocial.services.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatServiceImpl implements ChatService {

    static final int PAGE_SIZE = 30;
    static final String CURSOR_SEP = "_";

    ConversationRepository conversationRepository;
    ConversationMemberRepository conversationMemberRepository;
    MessageRepository messageRepository;
    AccountRepository accountRepository;
    ChatMapper chatMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForUser(String userId) {
        List<String> conversationIds = conversationMemberRepository.findConversationIdsByUserId(userId);
        if (conversationIds.isEmpty()) return List.of();

        List<Conversation> conversations = conversationRepository.findAllById(conversationIds);

        return conversations.stream()
                .map(this::toConversationDTOWithDetails)
                .sorted(Comparator.comparing(
                        (ConversationDTO c) -> c.getLastMessage() == null ? LocalDateTime.MIN : c.getLastMessage().getCreatedAt())
                        .reversed())
                .toList();
    }

    private ConversationDTO toConversationDTOWithDetails(Conversation conversation) {
        ConversationDTO dto = chatMapper.toConversationDTO(conversation);

        List<String> memberIds = conversationMemberRepository.findUserIdsByConversationId(conversation.getId());
        dto.setMembers(accountRepository.findOwnersByIdIn(memberIds));

        Message last = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());
        dto.setLastMessage(last == null ? null : chatMapper.toMessageDTO(last));

        return dto;
    }

    @Override
    @Transactional
    public ConversationDTO createConversation(String userId, CreateConversationRequest request) {
        List<String> memberIds = request.getMemberIds();
        if (memberIds == null || memberIds.isEmpty()) {
            throw new AppException("Member list must not be empty", StatusCode.PARAMATER_NOT_FOUND);
        }

        ConversationType type = request.getType() == null ? ConversationType.DIRECT : request.getType();

        if (type == ConversationType.DIRECT) {
            String otherUserId = memberIds.get(0);
            List<String> pair = List.of(userId, otherUserId);
            List<String> existing = conversationMemberRepository.findConversationIdsByExactMembers(pair, 2);
            for (String conversationId : existing) {
                Conversation candidate = conversationRepository.findById(conversationId).orElse(null);
                if (candidate != null && candidate.getType() == ConversationType.DIRECT) {
                    return toConversationDTOWithDetails(candidate);
                }
            }
        }

        Conversation conversation = conversationRepository.save(Conversation.builder()
                .type(type)
                .name(type == ConversationType.GROUP ? request.getName() : null)
                .build());

        List<String> allMemberIds = java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(userId), memberIds.stream())
                .distinct()
                .toList();

        for (String memberId : allMemberIds) {
            conversationMemberRepository.save(ConversationMember.builder()
                    .conversation(conversation)
                    .userId(memberId)
                    .role(memberId.equals(userId) ? com.fsocial.enums.MemberRole.OWNER : com.fsocial.enums.MemberRole.MEMBER)
                    .build());
        }

        return toConversationDTOWithDetails(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(String conversationId, String userId, String cursor) {
        requireMember(conversationId, userId);

        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        List<Message> messages = (cursor == null || cursor.isBlank())
                ? messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                : findAfterCursor(conversationId, cursor, pageable);

        return messages.stream().map(chatMapper::toMessageDTO).toList();
    }

    private List<Message> findAfterCursor(String conversationId, String cursor, Pageable pageable) {
        int sep = cursor.lastIndexOf(CURSOR_SEP);
        if (sep <= 0) return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        try {
            LocalDateTime before = LocalDateTime.ofEpochSecond(
                    Long.parseLong(cursor.substring(0, sep)) / 1000, 0, ZoneOffset.UTC);
            return messageRepository.findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(conversationId, before, pageable);
        } catch (NumberFormatException e) {
            return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        }
    }

    @Override
    @Transactional
    public MessageDTO sendMessage(String userId, SendMessageRequest request) {
        requireMember(request.getConversationId(), userId);

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new AppException(StatusCode.MESSAGE_SEND_FAILED);
        }

        Conversation conversationRef = conversationRepository.getReferenceById(request.getConversationId());
        Message replyTo = request.getReplyToId() == null ? null : messageRepository.getReferenceById(request.getReplyToId());
        Message message = messageRepository.save(Message.builder()
                .conversation(conversationRef)
                .senderId(userId)
                .content(request.getContent())
                .messageType(request.getMessageType() == null ? com.fsocial.enums.MessageType.TEXT : request.getMessageType())
                .replyTo(replyTo)
                .build());

        return chatMapper.toMessageDTO(message);
    }

    @Override
    public List<String> getMemberIds(String conversationId) {
        return conversationMemberRepository.findUserIdsByConversationId(conversationId);
    }

    private void requireMember(String conversationId, String userId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new AppException(StatusCode.CONVERSATION_NOT_FOUND);
        }
        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new AppException(StatusCode.NOT_CONVERSATION_MEMBER);
        }
    }
}
