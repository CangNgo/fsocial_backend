package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/check")
public class CheckConnection {
    @GetMapping
    public ApiResponse<String> checkConnection() {
        return ApiResponse.<String>builder()
                .message("Connect success")
                .build();
    }
}
