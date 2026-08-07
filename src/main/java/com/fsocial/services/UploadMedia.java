package com.fsocial.services;

import com.fsocial.dto.post.MediaItemDTO;
import com.fsocial.enums.AttachmentType;
import org.springframework.web.multipart.MultipartFile;

public interface UploadMedia {
    MediaItemDTO[] uploadMedia(MultipartFile[] files);
    MediaItemDTO[] uploadMedia(MultipartFile[] files, AttachmentType type);
    MediaItemDTO uploadSingleMedia(MultipartFile file);
    MediaItemDTO uploadSingleMedia(MultipartFile file, AttachmentType type);
}
