package com.fsocial.controller;

import com.fsocial.services.AIChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class AIChatController {
    AIChatService aiChatService;

    @GetMapping("/ai")
    String generation(@RequestParam String prompt) {
        return aiChatService.chatService(prompt);
    }
}
