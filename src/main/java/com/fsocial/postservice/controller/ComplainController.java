package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.complaint.ComplaintDTO;
import com.fsocial.postservice.dto.complaint.ComplaintDTOResponse;
import com.fsocial.postservice.dto.complaint.ComplaintStatisticsDTO;
import com.fsocial.postservice.dto.complaint.ComplaintStatisticsLongDayDTO;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.mapper.ComplantMapper;
import com.fsocial.postservice.services.ComplaintService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/complaint")
@Slf4j
public class ComplainController {

    ComplaintService complaintService;

    @PostMapping
    public ApiResponse<ComplaintDTO> addComplaint(@RequestBody @Valid ComplaintDTO complaint) throws AppCheckedException {
        ComplaintDTO complaintDTO = complaintService.addComplaint(complaint);
        return ApiResponse.<ComplaintDTO>builder()
                .data(complaintDTO)
                .message("Báo cáo bài viết thành công")
                .dateTime(LocalDateTime.now())
                .build();
    }

    @PutMapping("/reading")
    public ApiResponse<ComplaintDTO> updateComplaint(@RequestParam("complaint_id") String complaintId) throws AppCheckedException {
        return ApiResponse.<ComplaintDTO>builder()
                .data(complaintService.readComplaint(complaintId))
                .message("Cập nhật trạng thái đã đọc báo cáo thành công")
                .build();
    }

}
