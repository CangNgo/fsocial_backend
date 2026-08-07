package com.fsocial.services;

import com.fsocial.dto.Attachments.AttachmentDTO;
import com.fsocial.entity.Attachments;
import com.fsocial.entity.Post;
import com.fsocial.enums.MediaType;
import org.springframework.data.domain.Limit;

import java.util.List;

public interface AttachmentsService {

    AttachmentDTO save(AttachmentDTO dto);
    Attachments linkToPost(String attachmentId, Post post, int ord);
//    List<AttachmentDTO> getAttachmentsByOwnerId(String ownerId, Pageable pageable) throws AppCheckedException;
//    List<AttachmentDTO> getAttachmentsByType(String ownerId,String fileType,  Pageable pageable) throws AppCheckedException;
    List<AttachmentDTO> findAttachmentByFileTypeAndOwnerId(MediaType fileType, String userId, Limit limit);
}
