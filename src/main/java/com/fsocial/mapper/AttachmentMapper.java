package com.fsocial.mapper;

import com.fsocial.dto.Attachments.AttachmentDTO;
import com.fsocial.entity.Attachments;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {
    AttachmentDTO toDTO(Attachments attachments);
    Attachments toEntity(AttachmentDTO dto);
    List<AttachmentDTO> toDTOs(List<Attachments> attachments);
}
