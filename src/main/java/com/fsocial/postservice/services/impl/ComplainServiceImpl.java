package com.fsocial.postservice.services.impl;//package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.complaint.ComplaintDTO;
import com.fsocial.postservice.dto.complaint.ComplaintDTOResponse;
import com.fsocial.postservice.dto.notification.NotificationDTO;
import com.fsocial.postservice.dto.response.AccountResponse;
import com.fsocial.postservice.entity.Complaint;
import com.fsocial.postservice.entity.ComplaintDetail;
import com.fsocial.postservice.entity.TermOfServices;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.mapper.ComplantMapper;
import com.fsocial.postservice.publisher.NotificationEvent;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.ComplaintRepository;
import com.fsocial.postservice.repository.TermRepository;
import com.fsocial.postservice.repository.httpClient.ProfileClient;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.ComplaintService;
import com.fsocial.postservice.util.DisplayNameUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ComplainServiceImpl implements ComplaintService {

    ComplaintRepository complaintRepository;

    ComplantMapper complantMapper;

    TermRepository termRepository;

    ProfileClient profileClient;

    AccountRepository accountRepository;

    AccountService accountService;

    NotificationEvent notificationEvent;

    @Override
    @Transactional
    public ComplaintDTOResponse addComplaint(ComplaintDTO complaintDTO, String userId) {
        ComplaintDetail detail = ComplaintDetail.builder()
                .userId(userId)
                .termOfServiceId(complaintDTO.getTermOfServiceId())
                .createDatetime(LocalDateTime.now())
                .build();

        Complaint complaint = complaintRepository
                .findByTargetIdAndComplaintType(complaintDTO.getTargetId(), complaintDTO.getComplaintType())
                .orElseGet(() -> Complaint.builder()
                        .targetId(complaintDTO.getTargetId())
                        .complaintType(complaintDTO.getComplaintType())
                        .build());

        // addDetail set cả 2 chiều; cascade ALL trên Complaint.details lo phần insert complaint_detail
        complaint.addDetail(detail);

        Complaint res = complaintRepository.save(complaint);

        notificationEvent.publishCreateNotification(new NotificationDTO(
                userId,
                userId,
                NotificationType.REPORT
        ));

        return ComplaintDTOResponse.builder()
                .id(res.getId())
                .targetId(res.getTargetId())
                .complaintType(res.getComplaintType())
                .isRead(res.isRead())
                .reportCount(res.getDetails().size())
                .build();
    }

    @Override
    public ComplaintDTO readComplaint(String complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new AppException("Không tìm thấy khiếu nại", StatusCode.COMPLAIN_NOT_FOUND));
        complaint.setRead(true);

        complaintRepository.save(complaint);

        return ComplaintDTO.builder()
                .targetId(complaint.getTargetId())
                .complaintType(complaint.getComplaintType())
                .build();
    }

    // Methods from timelineService
    @Override
    @Transactional(readOnly = true)
    public List<com.fsocial.postservice.dto.complaint.ComplaintDTOResponse> getComplaints() {
        return complaintRepository.findAll().stream()
                .flatMap(complaint -> complaint.getDetails().stream()
                        .map(detail -> mapToComplainResponse(complaint, detail)))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.fsocial.postservice.dto.complaint.ComplaintDTOResponse getComplaintById(String complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new AppException("Không tìm thấy báo cáo", StatusCode.COMPLAIN_NOT_FOUND));
        ComplaintDetail detail = complaint.getDetails().isEmpty() ? null : complaint.getDetails().get(0);
        return mapToComplainResponse(complaint, detail);
    }

    @Override
    public List<com.fsocial.postservice.dto.complaint.ComplaintStatisticsDTO> countStatisticsComplainToday(
            LocalDateTime startDate, LocalDateTime endDate) {
        List<com.fsocial.postservice.dto.complaint.ComplaintStatisticsDTO> complaintStatisticsDTOS = complaintRepository
                .countByCreatedAtByHours(startDate, endDate);
        List<com.fsocial.postservice.dto.complaint.ComplaintStatisticsDTO> result = new java.util.ArrayList<>();
        java.util.Map<String, Integer> mapComplaint = new java.util.HashMap<>();

        for (com.fsocial.postservice.dto.complaint.ComplaintStatisticsDTO complaintStatisticsDTO : complaintStatisticsDTOS) {
            String hour = complaintStatisticsDTO.getHour();
            Integer count = complaintStatisticsDTO.getCount();
            mapComplaint.put(hour, count);
        }

        // Bản cũ add 2 dòng cho giờ có dữ liệu (thiếu else) → 24 giờ ra tới 48 dòng.
        for (int hour = 0; hour < 24; hour++) {
            String key = String.valueOf(hour);
            result.add(new com.fsocial.postservice.dto.complaint.ComplaintStatisticsDTO(
                    key, mapComplaint.getOrDefault(key, 0)));
        }

        return result;
    }

    @Override
    public List<com.fsocial.postservice.dto.complaint.ComplaintStatisticsLongDayDTO> countStatisticsComplainLongDay(
            LocalDateTime startDate, LocalDateTime endDate) {
        // Bản cũ remove trong lúc for-each (ConcurrentModificationException) và bắn dòng 0
        // cho mọi phần tử không khớp. Index theo ngày rồi fill là đủ, mỗi ngày đúng 1 dòng.
        java.util.Map<LocalDateTime, Integer> countByDay = complaintRepository.countByDate(startDate, endDate)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        d -> d.getDate().truncatedTo(java.time.temporal.ChronoUnit.DAYS),
                        com.fsocial.postservice.dto.complaint.ComplaintStatisticsLongDayDTO::getCount,
                        Integer::sum));

        List<com.fsocial.postservice.dto.complaint.ComplaintStatisticsLongDayDTO> result = new java.util.ArrayList<>();
        LocalDateTime start = startDate.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        LocalDateTime end = endDate.plusDays(1).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        while (start.isBefore(end)) {
            result.add(new com.fsocial.postservice.dto.complaint.ComplaintStatisticsLongDayDTO(
                    start, countByDay.getOrDefault(start, 0)));
            start = start.plusDays(1);
        }
        return result;
    }

    private ComplaintDTOResponse mapToComplainResponse(Complaint complaint, ComplaintDetail detail) {
        String reportedByUserId = detail != null ? detail.getUserId() : null;
        AccountResponse profileResponse = getProfile(reportedByUserId);
        String termName = null;
        if (detail != null && detail.getTermOfServiceId() != null) {
            TermOfServices term = termRepository.findById(detail.getTermOfServiceId()).orElseThrow(
                    () -> new AppException("Không tìm thấy chính sách", StatusCode.TERM_OF_SERVICE_NOT_FOUND));
            termName = term.getName();
        }
        return ComplaintDTOResponse.builder()
                .id(complaint.getId())
                .targetId(complaint.getTargetId())
                .profileId(profileResponse.getId())
                .complaintType(complaint.getComplaintType())
                .isRead(complaint.isRead())
                .termOfService(termName)
                .createDatetime(detail != null ? detail.getCreateDatetime() : null)
                .displayName(profileResponse.getDisplayName())
                .userId(reportedByUserId)
                .reportCount(complaint.getDetails().size())
                .build();
    }

    public AccountResponse getProfile(String userId) {
        try {
            return accountService.getProfile(userId);
        } catch (Exception e) {
            throw new AppException("Không tìm thấy thông tin người dùng: " + userId, StatusCode.USER_NOT_FOUND);
        }
    }
}
