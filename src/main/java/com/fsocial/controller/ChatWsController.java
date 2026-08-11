package com.fsocial.controller;

import com.fsocial.dto.message.MessageDTO;
import com.fsocial.dto.message.SendMessageRequest;
import com.fsocial.services.ChatService;
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

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWsController {

    ChatService chatService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(@Payload SendMessageRequest request, Principal principal) {
        if (principal == null) {
            log.warn("STOMP chat.send rejected: no authenticated principal (CONNECT auth likely failed)");
            return;
        }
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
}
