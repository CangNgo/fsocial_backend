package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.Response;
import com.fsocial.postservice.entity.MediaItem;
import com.fsocial.postservice.enums.ResponseStatus;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.services.UploadMedia;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/upload_file")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileUploadController {;
    UploadMedia uploadImage;

    @PostMapping
    public ResponseEntity<Response> uploadFile(@RequestParam("fileUpload") MultipartFile[] file) throws AppCheckedException {
            MediaItem[] mediaItems = uploadImage.uploadMedia(file);
            log.info("Upload file successfull: {}", (Object) mediaItems);
            return ResponseEntity.ok().body(Response.builder()
                            .data(mediaItems)
                            .message("Upload file successful")
                            .dateTime(LocalDateTime.now())
                    .build());
    }

    @PostMapping("/messages")
    public ApiResponse<List<MediaItem>> uploadImageInMessage(@RequestParam MultipartFile[] images) throws AppCheckedException {
        MediaItem[] mediaItems = uploadImage.uploadMedia(images);
        return ApiResponse.buildApiResponse(Arrays.asList(mediaItems), ResponseStatus.SUCCESS);
    }
}
