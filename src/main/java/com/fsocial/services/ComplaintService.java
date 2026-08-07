package com.fsocial.services;

import com.fsocial.dto.complaint.ComplaintDTO;
import com.fsocial.dto.complaint.ComplaintDTOResponse;
import com.fsocial.dto.complaint.ComplaintStatisticsDTO;
import com.fsocial.dto.complaint.ComplaintStatisticsLongDayDTO;

import java.util.List;

public interface ComplaintService {
    ComplaintDTOResponse addComplaint(ComplaintDTO complaint, String userId);
    ComplaintDTO readComplaint(String complaintId);

    // Methods from timelineService
    List<ComplaintDTOResponse> getComplaints();
    ComplaintDTOResponse getComplaintById(String complaintId);
    List<ComplaintStatisticsDTO> countStatisticsComplainToday(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    List<ComplaintStatisticsLongDayDTO> countStatisticsComplainLongDay(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
