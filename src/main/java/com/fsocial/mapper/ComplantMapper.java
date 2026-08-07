package com.fsocial.mapper;

import com.fsocial.dto.complaint.ComplaintDTO;
import com.fsocial.entity.Complaint;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ComplantMapper {
    ComplaintDTO toComplaintDTO(Complaint complaint);
    Complaint toComplaint(ComplaintDTO complaintDTO);
    List<ComplaintDTO> toComplaintDTO(List<Complaint> complaints);
}
