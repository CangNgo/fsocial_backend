package com.fsocial.postservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.exception.StatusCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        StatusCode statusCode = StatusCode.UNAUTHENTICATED;

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .statusCode(statusCode.getCode())
                .message(statusCode.getMessage())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

