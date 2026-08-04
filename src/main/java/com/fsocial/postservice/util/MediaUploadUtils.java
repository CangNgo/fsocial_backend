package com.fsocial.postservice.util;

import com.fsocial.postservice.dto.post.MediaItemDTO;
import com.fsocial.postservice.enums.AttachmentType;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.services.UploadMedia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaUploadUtils {

    private final UploadMedia uploadMedia;

    public MediaItemDTO[] uploadValidMedia(MultipartFile[] media) {
        return uploadValidMedia(media, null);
    }

    public MediaItemDTO[] uploadValidMedia(MultipartFile[] media, AttachmentType type) {
        if (media == null || media.length == 0) return new MediaItemDTO[0];

        if (!hasValidMedia(media)) return new MediaItemDTO[0];

        try {
            return uploadMedia.uploadMedia(media, type);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi tải lên tệp: {}", e.getMessage(), e);
            throw new AppException("Upload hình ảnh thất bại", StatusCode.UPLOAD_MEDIA_FAILED);
        }
    }

    private boolean hasValidMedia(MultipartFile[] media) {
        for (MultipartFile file : media) {
            if (isValid(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValid(MultipartFile file) {
        return file != null
                && !file.isEmpty()
                && file.getOriginalFilename() != null
                && !file.getOriginalFilename().isEmpty();
    }
}
