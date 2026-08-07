//package com.fsocial.postservice.services.impl;
//
//import com.fsocial.postservice.dto.complaint.*;
//import com.fsocial.postservice.entity.Account;
//import com.fsocial.postservice.entity.Complaint;
//import com.fsocial.postservice.entity.TermOfServices;
//import com.fsocial.postservice.exception.AppCheckedException;
//import com.fsocial.postservice.exception.StatusCode;
//import com.fsocial.postservice.mapper.ComplantMapper;
//import com.fsocial.postservice.repository.AccountRepository;
//import com.fsocial.postservice.repository.ComplaintRepository;
//import com.fsocial.postservice.repository.TermOfServicesRepository;
//import com.fsocial.postservice.services.ComplaintService;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Slf4j
//public class ComplaintServiceImpl implements ComplaintService {
//
//    ComplaintRepository complaintRepository;
//    AccountRepository accountRepository;
//    TermOfServicesRepository termOfServicesRepository;
//    ComplantMapper complantMapper;
//
//    @Override
//    public ComplaintDTO addComplaint(ComplaintDTO complaintDTO) throws AppCheckedException {
//        Complaint complaint = complantMapper.toComplaint(complaintDTO);
//        complaint.setCreateDatetime(LocalDateTime.now());
//        complaint.setReadding(false);
//        return complantMapper.toComplaintDTO(complaintRepository.save(complaint));
//    }
//
//    @Override
//    public ComplaintDTO readComplaint(String complaintId) throws AppCheckedException {
//        Complaint complaint = complaintRepository.findById(complaintId)
//                .orElseThrow(() -> new AppCheckedException("Không tìm thấy báo cáo", StatusCode.COMPLAIN_NOT_FOUND));
//        complaint.setReadding(true);
//        return complantMapper.toComplaintDTO(complaintRepository.save(complaint));
//    }
//
//    @Override
//    public List<ComplaintDTOResponse> getComplaints() {
//        List<Complaint> complaints = complaintRepository.findAll();
//        return toComplaintResponses(complaints);
//    }
//
//    @Override
//    public ComplaintDTOResponse getComplaintById(String complaintId) throws AppCheckedException {
//        Complaint complaint = complaintRepository.findById(complaintId)
//                .orElseThrow(() -> new AppCheckedException("Không tìm thấy báo cáo", StatusCode.COMPLAIN_NOT_FOUND));
//        return toComplaintResponse(complaint);
//    }
//
//    @Override
//    public List<ComplaintStatisticsDTO> countStatisticsComplainToday(LocalDateTime startDate, LocalDateTime endDate) {
//        return complaintRepository.countByCreatedAtByHours(startDate, endDate);
//    }
//
//    @Override
//    public List<ComplaintStatisticsLongDayDTO> countStatisticsComplainLongDay(LocalDateTime startDate, LocalDateTime endDate) {
//        List<ComplaintStatisticsLongDayDTO> rawData = complaintRepository.countByDate(startDate, endDate);
//        List<ComplaintStatisticsLongDayDTO> result = new ArrayList<>();
//        LocalDateTime start = startDate.truncatedTo(ChronoUnit.DAYS);
//        LocalDateTime end = endDate.plusDays(1).truncatedTo(ChronoUnit.DAYS);
//
//        while (!start.equals(end)) {
//            LocalDateTime current = start;
//            boolean found = false;
//            for (ComplaintStatisticsLongDayDTO item : rawData) {
//                if (item.getDate() != null && item.getDate().truncatedTo(ChronoUnit.DAYS).equals(current)) {
//                    result.add(item);
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                result.add(new ComplaintStatisticsLongDayDTO(current, 0));
//            }
//            start = start.plusDays(1);
//        }
//        return result;
//    }
//
//    private List<ComplaintDTOResponse> toComplaintResponses(List<Complaint> complaints) {
//        if (complaints.isEmpty()) return List.of();
//
//        List<String> userIds = complaints.stream().map(Complaint::getUserId).distinct().toList();
//        List<String> termIds = complaints.stream().map(Complaint::getTermOfServiceId).distinct().toList();
//
//        Map<String, Account> accountMap = accountRepository.findAllById(userIds).stream()
//                .collect(Collectors.toMap(Account::getId, Function.identity()));
//        Map<String, TermOfServices> termMap = termOfServicesRepository.findAllById(termIds).stream()
//                .collect(Collectors.toMap(TermOfServices::getId, Function.identity()));
//
//        return complaints.stream()
//                .map(c -> toComplaintResponse(c, accountMap, termMap))
//                .collect(Collectors.toList());
//    }
//
//    private ComplaintDTOResponse toComplaintResponse(Complaint complaint) {
//        Account account = accountRepository.findById(complaint.getUserId()).orElse(null);
//        TermOfServices term = termOfServicesRepository.findById(complaint.getTermOfServiceId()).orElse(null);
//        return buildResponse(complaint, account, term);
//    }
//
//    private ComplaintDTOResponse toComplaintResponse(Complaint complaint,
//                                                      Map<String, Account> accountMap,
//                                                      Map<String, TermOfServices> termMap) {
//        Account account = accountMap.get(complaint.getUserId());
//        TermOfServices term = termMap.get(complaint.getTermOfServiceId());
//        return buildResponse(complaint, account, term);
//    }
//
//    private ComplaintDTOResponse buildResponse(Complaint complaint, Account account, TermOfServices term) {
//        return ComplaintDTOResponse.builder()
//                .id(complaint.getId())
//                .postId(complaint.getPostId())
//                .userId(complaint.getUserId())
//                .firstName(account != null ? account.getFirstName() : null)
//                .lastName(account != null ? account.getLastName() : null)
//                .complaintType(complaint.getComplaintType())
//                .termOfService(term != null ? term.getName() : null)
//                .createDatetime(complaint.getCreateDatetime())
//                .readding(complaint.isReadding())
//                .build();
//    }
//}
