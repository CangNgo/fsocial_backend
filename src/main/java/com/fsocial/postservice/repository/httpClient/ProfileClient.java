package com.fsocial.postservice.repository.httpClient;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.request.ProfileRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.profile}")
    private String profileServiceUrl;

    public void createProfile(ProfileRegisterRequest request) {
        try {
            restTemplate.postForObject(
                    profileServiceUrl + "/internal/create",
                    request,
                    Void.class
            );
        } catch (Exception e) {
            log.error("Lỗi khi tạo profile cho userId={}: {}", request.getUserId(), e.getMessage());
            throw e;
        }
    }

    public ApiResponse<Map<String, List<String>>> listFollowing(String userId) {
        ResponseEntity<ApiResponse<Map<String, List<String>>>> response = restTemplate.exchange(
                profileServiceUrl + "/internal/list-following/" + userId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }
}
