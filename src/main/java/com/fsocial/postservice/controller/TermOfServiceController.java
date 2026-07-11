package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.termOfService.TermOfServiceDTO;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.services.TermOfServicesService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/term_of_service")
public class TermOfServiceController {

    TermOfServicesService termOfServicesService;

    @PostMapping
    public ApiResponse<TermOfServiceDTO> addTermOfSerivce(@RequestBody TermOfServiceDTO termOfServiceDTO) {
        TermOfServiceDTO termOfService = termOfServicesService.addTermOfService(termOfServiceDTO);
        return ApiResponse.<TermOfServiceDTO>builder()
                .message("Thêm chính sách mới thành công")
                .data(termOfService)
                .build();
    }

    @PutMapping
    public ApiResponse<TermOfServiceDTO> updateTermOfSerivce(@RequestBody TermOfServiceDTO termOfServiceDTO) throws AppCheckedException {
        TermOfServiceDTO termOfService = termOfServicesService.updateTermOfService(termOfServiceDTO);
        return ApiResponse.<TermOfServiceDTO>builder()
                .message("Cập nhật chính sách thành công")
                .data(termOfService)
                .build();
    }

    @DeleteMapping
    public ApiResponse<String> deleteTermOfSerivce(@RequestParam("term_id") String termId) throws AppCheckedException {

        return ApiResponse.<String>builder()
                .message("Xóa chính sách thành công")
                .data(termOfServicesService.deleteTermOfService(termId))
                .build();
    }


    @GetMapping
    public ApiResponse<List<TermOfServiceDTO>> getTermOfService() {

        List<TermOfServiceDTO> res =termOfServicesService.getTermOfServices();

        return ApiResponse.<List<TermOfServiceDTO>>builder()
                .data(res)
                .message("Lấy toàn bộ danh sách chính sách thành công")
                .build();
    }
}
