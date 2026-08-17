package com.fsocial.controller;

import com.fsocial.dto.message.MarkReadDTO;
import com.fsocial.dto.message.MessageDTO;
import com.fsocial.dto.message.SendMessageRequest;
import com.fsocial.services.ChatService;
import com.fsocial.services.impl.OnlineServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.fsocial.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWsController {

    ChatService chatService;
    SimpMessagingTemplate messagingTemplate;
    OnlineServiceImpl onlineService;

    @MessageMapping("/chat.send")
    public void send(@Payload SendMessageRequest request, Principal principal) {
        try {
            MessageDTO saved = chatService.sendMessage(principal.getName(), request);
            for (String memberId : chatService.getMemberIds(request.getConversationId())) {
                messagingTemplate.convertAndSendToUser(memberId, "/queue/messages", saved);
            }
        } catch (AppException e) {
            log.warn("STOMP chat.send failed for user {}: {}", principal.getName(), e.getMessage());
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", e.getMessage());
        } catch (Exception e) {
            log.error("STOMP chat.send unexpected error for user {}", principal.getName(), e);
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", "Send failed");
        }
    }

    @MessageMapping("/chat/mark-read")
    public void markRead(@Payload MarkReadDTO request, Principal principal){
        try {
            chatService.markReadConversation(request, principal.getName());
        } catch (AppException e) {
            log.warn("STOMP chat.send failed for user {}: {}", principal.getName(), e.getMessage());
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", e.getMessage());
        } catch (Exception e) {
            log.error("STOMP chat.send unexpected error for user {}", principal.getName(), e);
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", "Send failed");
        }
    }

    @MessageMapping("/online/mark-online")
    public void markOnline(@Payload List<String> userIds, Principal principal){
        onlineService.setOnline(principal.getName());
        // send list conversation online
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/conversation/online",
                chatService.listConversationOnline(userIds));
    }
}
