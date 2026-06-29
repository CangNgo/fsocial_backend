package com.fsocial.postservice.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.fsocial.postservice.dto.Attachments.AttachmentDTO;
import com.fsocial.postservice.entity.MediaItem;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.services.AttachmentsService;
import com.fsocial.postservice.services.UploadMedia;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UploadMediaImpl implements UploadMedia {
    Cloudinary cloudinary;
    AttachmentsService attachmentsService;
    private static final int BUFFER_SIZE = 8192; // Tăng buffer size để upload nhanh hơn
    private static final long MAX_CLOUDINARY_FILE_SIZE = 10L * 1024 * 1024;
    private static final String CLOUDINARY_FILE_TOO_LARGE = "file size too large";
    private static final String[] SUPPORTED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif"};
    private static final String[] SUPPORTED_VIDEO_TYPES = {"mp4", "mov", "avi", "wmv"};
    private static final String FILE_TOO_LARGE_MESSAGE = "Tệp %s vượt quá giới hạn 10MB, vui lòng chọn tệp nhỏ hơn.";
    private static final String PARTIAL_UPLOAD_FAILED_MESSAGE = "Một số tệp tải lên thất bại. Vui lòng kiểm tra lại các tệp lỗi.";
    private static final String ALL_UPLOAD_FAILED_MESSAGE = "Tất cả tệp tải lên đều thất bại. Vui lòng kiểm tra lại dung lượng và định dạng tệp.";
    private static final String FILE_NAME_FALLBACK = "unknown";
    private static final String UPLOAD_ERROR_MESSAGE = "Upload failed";
    private static final String SECURE_URL_KEY = "secure_url";
    private static final String PUBLIC_ID_KEY = "public_id";
    private static final String BYTES_KEY = "bytes";
    private static final String RESOURCE_TYPE_KEY = "resource_type";
    private static final String FORMAT_KEY = "format";
    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";

    @Override
    public MediaItem[] uploadMedia(MultipartFile[] files) throws AppCheckedException {
        if (files == null || files.length == 0) {
            throw new AppCheckedException("No files provided", StatusCode.FILE_NOT_FOUND);
        }
        String userId = currentUserId();
        MediaItem[] mediaItems = new MediaItem[files.length];
        int successCount = 0;
        boolean hasOversizeFailure = false;

        for (int i = 0; i < files.length; i++) {
            MultipartFile currentFile = files[i];
            if (currentFile == null || currentFile.isEmpty()) {
                continue;
            }
            try {
                mediaItems[i] = uploadOne(currentFile, userId);
                successCount++;
            } catch (AppCheckedException e) {
                mediaItems[i] = null;
                if (e.getStatus() == StatusCode.FILE_TOO_LARGE) {
                    hasOversizeFailure = true;
                }
                log.warn("Upload file thất bại tại index {} cho file {}: {}", i, safeFileName(currentFile), e.getMessage());
            }
        }

        if (successCount == 0) {
            throw new AppCheckedException(hasOversizeFailure
                    ? "Tất cả tệp đều vượt quá giới hạn 10MB. Vui lòng chọn tệp tin nhỏ hơn."
                    : ALL_UPLOAD_FAILED_MESSAGE,
                    hasOversizeFailure ? StatusCode.FILE_TOO_LARGE : StatusCode.UPLOAD_MEDIA_FAILED);
        }

        if (successCount < countValidFiles(files)) {
            log.warn(PARTIAL_UPLOAD_FAILED_MESSAGE);
        }

        return mediaItems;
    }

    @Override
    public MediaItem uploadSingleMedia(MultipartFile file) throws AppCheckedException {
        if (file == null || file.isEmpty()) {
            throw new AppCheckedException("No file provided", StatusCode.FILE_NOT_FOUND);
        }
        return uploadOne(file, currentUserId());
    }

    private MediaItem uploadOne(MultipartFile file, String userId) throws AppCheckedException {
        File tempFile = null;
        try {
            validateFileSize(file);
            String[] fileParts = extractFileParts(file.getOriginalFilename());
            String publicId = generatePublicId(fileParts[0]);
            String extension = fileParts[1].toLowerCase(Locale.ROOT);
            String resourceType = determineResourceType(extension);

            tempFile = convertToFile(file, extension);
            Map<String, Object> uploadParams = configureUploadParams(resourceType);
            uploadParams.put(PUBLIC_ID_KEY, publicId);
            uploadParams.put(RESOURCE_TYPE_KEY, resourceType);
            uploadParams.put("overwrite", true);
            uploadParams.put("invalidate", true);

            Map uploadResult = cloudinary.uploader().upload(tempFile, uploadParams);

            String secureUrl = uploadResult.get(SECURE_URL_KEY).toString();
            String resolvedType = uploadResult.get(RESOURCE_TYPE_KEY).toString();
            attachmentsService.save(AttachmentDTO.builder()
                    .publicId(uploadResult.get(PUBLIC_ID_KEY).toString())
                    .size(uploadResult.get(BYTES_KEY).toString())
                    .resourceType(resolvedType)
                    .fileType(uploadResult.get(FORMAT_KEY).toString())
                    .ownerId(userId)
                    .url(secureUrl)
                    .build());

            Object rawWidth = uploadResult.get(WIDTH_KEY);
            Object rawHeight = uploadResult.get(HEIGHT_KEY);
            return MediaItem.builder()
                    .url(secureUrl)
                    .type(resolvedType)
                    .width(rawWidth != null ? Integer.parseInt(rawWidth.toString()) : null)
                    .height(rawHeight != null ? Integer.parseInt(rawHeight.toString()) : null)
                    .build();
        } catch (AppCheckedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error uploading file {}: {}", safeFileName(file), e.getMessage(), e);
            if (isFileTooLargeException(e)) {
                throw new AppCheckedException(buildFileTooLargeMessage(file), StatusCode.FILE_TOO_LARGE);
            }
            throw new AppCheckedException(UPLOAD_ERROR_MESSAGE + ": " + e.getMessage(), StatusCode.INTERNAL_SERVER_ERROR);
        } finally {
            cleanUpTempFile(tempFile);
        }
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Map<String, Object> configureUploadParams(String resourceType) {
        if ("image".equals(resourceType)) {
            return ObjectUtils.asMap(
                    "quality", "auto",
                    "format", "png",
                    "transformation", new Transformation<>().quality("auto").fetchFormat("png")
            );
        } else if ("video".equals(resourceType)) {
            return ObjectUtils.asMap(
                    "video_codec", "h264",     // Sử dụng codec H.264
                    "quality", "auto"            // Giới hạn chất lượng video ở 70%
            );
        }
        return ObjectUtils.emptyMap(); // Mặc định nếu không xác định được
    }

    private String determineResourceType(String extension) throws AppCheckedException {
        if (Arrays.stream(SUPPORTED_IMAGE_TYPES).anyMatch(extension::equals)) {
            return "image";
        } else if (Arrays.stream(SUPPORTED_VIDEO_TYPES).anyMatch(extension::equals)) {
            return "video";
        }
        throw new AppCheckedException("Unsupported file type: " + extension, StatusCode.UNSUPPORTED_MEDIA_TYPE);
    }

    private void validateFileSize(MultipartFile file) throws AppCheckedException {
        if (file.getSize() > MAX_CLOUDINARY_FILE_SIZE) {
            throw new AppCheckedException(buildFileTooLargeMessage(file), StatusCode.FILE_TOO_LARGE);
        }
    }

    private boolean isFileTooLargeException(Exception exception) {
        return exception.getMessage() != null
                && exception.getMessage().toLowerCase(Locale.ROOT).contains(CLOUDINARY_FILE_TOO_LARGE);
    }

    private String buildFileTooLargeMessage(MultipartFile file) {
        return String.format(FILE_TOO_LARGE_MESSAGE, safeFileName(file));
    }

    private String safeFileName(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            return FILE_NAME_FALLBACK;
        }
        return file.getOriginalFilename();
    }

    private int countValidFiles(MultipartFile[] files) {
        int count = 0;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private File convertToFile(MultipartFile file, String extension) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), "." + extension);

        try (InputStream in = file.getInputStream();
             FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private String generatePublicId(String fileName) {
        return "media_" + UUID.randomUUID().toString() + "_" + fileName;
    }

    private String[] extractFileParts(String originalName) throws AppCheckedException {
        if (originalName == null || !originalName.contains(".")) {
            throw new AppCheckedException("Invalid filename", StatusCode.UNSUPPORTED_MEDIA_TYPE);
        }
        int dotIndex = originalName.lastIndexOf('.');
        String name = originalName.substring(0, dotIndex);
        String extension = originalName.substring(dotIndex + 1);
        return new String[]{name, extension};
    }

    private void cleanUpTempFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: {}", e.getMessage());
            }
        }
    }
}