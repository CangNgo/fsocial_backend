package com.fsocial.services;

import com.fsocial.dto.ApiDataCursor;
import com.fsocial.dto.Attachments.AttachementProfileDTO;
import com.fsocial.dto.Attachments.AttachmentDTO;
import com.fsocial.dto.page.AttachmentsRequest;
import com.fsocial.entity.Attachments;
import com.fsocial.entity.Post;
import org.springframework.data.domain.Limit;

import java.util.List;

public interface AttachmentsService {

    AttachmentDTO save(AttachmentDTO dto);

    Attachments linkToPost(String attachmentId, Post post, int ord);

    //    List<AttachmentDTO> getAttachmentsByOwnerId(String ownerId, Pageable pageable) throws AppCheckedException;
//    List<AttachmentDTO> getAttachmentsByType(String ownerId,String fileType,  Pageable pageable) throws AppCheckedException;
    ApiDataCursor<AttachmentDTO> getAttachmentByOwnerId(AttachmentsRequest dto, String userId);
}
