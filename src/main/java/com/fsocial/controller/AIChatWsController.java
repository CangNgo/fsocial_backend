package com.fsocial.controller;

import com.fsocial.dto.ai.AiChatReply;
import com.fsocial.dto.ai.AiChatRequest;
import com.fsocial.services.AIChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.concurrent.Executor;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AIChatWsController {

    static int MAX_PROMPT_LENGTH = 1000;
    static String QUEUE_AI = "/queue/ai";
    static String ERROR_TEXT = "Có lỗi xảy ra, vui lòng thử lại.";

    AIChatService aiChatService;
    SimpMessagingTemplate messagingTemplate;
    Executor aiChatExecutor;

    /**
     * Gọi LLM mất vài giây. Chạy thẳng trên clientInboundChannel sẽ chiếm thread của
     * pool dùng chung với chat.send, vài người hỏi AI cùng lúc là tin nhắn thường bị
     * nghẽn theo. Vì vậy trả thread về ngay và đẩy kết quả qua executor riêng.
     */
    @MessageMapping("/ai.ask")
    public void ask(@Payload AiChatRequest request, Principal principal) {
        if (principal == null) return;

        String userId = principal.getName();
        String requestId = request.getRequestId();
        String prompt = request.getPrompt() == null ? "" : request.getPrompt().trim();

        if (prompt.isEmpty() || prompt.length() > MAX_PROMPT_LENGTH) {
            sendReply(userId, requestId, ERROR_TEXT, false);
            return;
        }

        aiChatExecutor.execute(() -> {
            try {
                String content = aiChatService.chatService(prompt);
                if (content == null || content.isBlank()) {
                    sendReply(userId, requestId, ERROR_TEXT, false);
                    return;
                }
                sendReply(userId, requestId, content, true);
            } catch (Exception e) {
                log.error("STOMP ai.ask failed for user {}", userId, e);
                sendReply(userId, requestId, ERROR_TEXT, false);
            }
        });
    }

    private void sendReply(String userId, String requestId, String content, boolean success) {
        messagingTemplate.convertAndSendToUser(userId, QUEUE_AI, AiChatReply.builder()
                .requestId(requestId)
                .content(content)
                .success(success)
                .build());
    }
}
