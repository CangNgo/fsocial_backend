package com.fsocial.controller;

import com.fsocial.dto.ApiResponse;
import com.fsocial.dto.faq.FaqDTO;
import com.fsocial.dto.faq.FaqRatingRequest;
import com.fsocial.dto.faq.FaqRequest;
import com.fsocial.enums.FaqType;
import com.fsocial.services.FaqService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/faq")
public class FaqController {

    FaqService faqService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FaqDTO> createFaq(@RequestBody @Valid FaqRequest request) {
        return ApiResponse.<FaqDTO>builder()
                .message("Tạo hướng dẫn thành công")
                .data(faqService.createFaq(request))
                .build();
    }

    @PutMapping("/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FaqDTO> updateFaq(@PathVariable String faqId, @RequestBody @Valid FaqRequest request) {
        return ApiResponse.<FaqDTO>builder()
                .message("Cập nhật hướng dẫn thành công")
                .data(faqService.updateFaq(faqId, request))
                .build();
    }

    @DeleteMapping("/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteFaq(@PathVariable String faqId) {
        faqService.deleteFaq(faqId);
        return ApiResponse.<Void>builder()
                .message("Xóa hướng dẫn thành công")
                .build();
    }

    @GetMapping
    public ApiResponse<List<FaqDTO>> getAllFaqs() {
        return ApiResponse.<List<FaqDTO>>builder()
                .message("Lấy danh sách hướng dẫn thành công")
                .data(faqService.getAllFaqs())
                .build();
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<FaqDTO>> getFaqsByType(@PathVariable FaqType type) {
        return ApiResponse.<List<FaqDTO>>builder()
                .message("Lấy danh sách hướng dẫn theo loại thành công")
                .data(faqService.getFaqsByType(type))
                .build();
    }

    @GetMapping("/{faqId}")
    public ApiResponse<FaqDTO> getFaqById(@PathVariable String faqId, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<FaqDTO>builder()
                .message("Lấy chi tiết hướng dẫn thành công")
                .data(faqService.getFaqById(faqId, jwt.getSubject()))
                .build();
    }

    @PostMapping("/{faqId}/rating")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<FaqDTO> rateFaq(@PathVariable String faqId, @RequestBody @Valid FaqRatingRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<FaqDTO>builder()
                .message("Đánh giá hướng dẫn thành công")
                .data(faqService.rateFaq(faqId, jwt.getSubject(), request))
                .build();
    }
}
