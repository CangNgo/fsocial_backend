package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.post.MediaItemDTO;
import com.fsocial.postservice.enums.AttachmentType;
import org.springframework.web.multipart.MultipartFile;

public interface UploadMedia {
    MediaItemDTO[] uploadMedia(MultipartFile[] files);
    MediaItemDTO[] uploadMedia(MultipartFile[] files, AttachmentType type);
    MediaItemDTO uploadSingleMedia(MultipartFile file);
    MediaItemDTO uploadSingleMedia(MultipartFile file, AttachmentType type);
}
