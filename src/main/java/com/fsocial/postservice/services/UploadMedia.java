package com.fsocial.postservice.services;

import com.fsocial.postservice.entity.MediaItem;
import org.springframework.web.multipart.MultipartFile;

public interface UploadMedia {
    MediaItem[] uploadMedia(MultipartFile[] files);
    MediaItem uploadSingleMedia(MultipartFile file);
}
