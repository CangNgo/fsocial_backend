package com.fsocial.controller;

import com.fsocial.dto.ApiResponse;
import com.fsocial.dto.post.MediaItemDTO;
import com.fsocial.services.UploadMedia;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/internal")
@Slf4j
public class InternalApi {
    UploadMedia uploadImage;

    @PostMapping("/upload-file")
    public ApiResponse<MediaItemDTO> uploadFile(@RequestParam("fileUpload") MultipartFile file) {
        MediaItemDTO[] items = uploadImage.uploadMedia(new MultipartFile[]{file});
        return ApiResponse.<MediaItemDTO>builder()
                .data(items[0])
                .message("Upload file success")
                .build();
    }

    @PostMapping("/upload-files")
    public ApiResponse<List<MediaItemDTO>> uploadFiles(@RequestParam("fileUpload") MultipartFile[] file) {
        MediaItemDTO[] items = uploadImage.uploadMedia(file);
        return ApiResponse.<List<MediaItemDTO>>builder()
                .data(Arrays.asList(items))
                .message("Upload file success")
                .build();
    }
}
