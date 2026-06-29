package com.fsocial.postservice.services;

import com.fsocial.postservice.entity.MediaItem;
import com.fsocial.postservice.exception.AppCheckedException;
import org.springframework.web.multipart.MultipartFile;

public interface UploadMedia {
    MediaItem[] uploadMedia(MultipartFile[] files) throws AppCheckedException;
    MediaItem uploadSingleMedia(MultipartFile file) throws AppCheckedException;
}
