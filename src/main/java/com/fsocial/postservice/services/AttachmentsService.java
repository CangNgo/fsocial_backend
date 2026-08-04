package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.Attachments.AttachmentDTO;
import com.fsocial.postservice.entity.Attachments;
import com.fsocial.postservice.entity.Post;

public interface AttachmentsService {

    AttachmentDTO save(AttachmentDTO dto);
    Attachments linkToPost(String attachmentId, Post post, int ord);
//    List<AttachmentDTO> getAttachmentsByOwnerId(String ownerId, Pageable pageable) throws AppCheckedException;
//    List<AttachmentDTO> getAttachmentsByType(String ownerId,String fileType,  Pageable pageable) throws AppCheckedException;

}
