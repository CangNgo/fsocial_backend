package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.relationships.FollowRequest;
import com.fsocial.postservice.enums.ResponseStatus;
import com.fsocial.postservice.services.RelationshipService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelationshipController {
    RelationshipService relationshipService;

    @PostMapping("/follow")
    public ApiResponse<Void> follow(@RequestBody @Valid FollowRequest request) {
        relationshipService.follow(request.getUserId(), request.getTargetId());
        return ApiResponse.<Void>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    @PostMapping("/unfollow")
    public ApiResponse<Void> unfollow(@RequestBody @Valid FollowRequest request) {
        relationshipService.unfollow(request.getUserId(), request.getTargetId());
        return ApiResponse.<Void>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    @GetMapping("/followers/{userId}")
    public ApiResponse<Set<String>> getFollowers(@PathVariable String userId) {
        return ApiResponse.<Set<String>>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .data(relationshipService.getFollowers(userId))
                .build();
    }

    @GetMapping("/followings/{userId}")
    public ApiResponse<Set<String>> getFollowings(@PathVariable String userId) {
        return ApiResponse.<Set<String>>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .data(relationshipService.getFollowing(userId))
                .build();
    }
}
