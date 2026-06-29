package com.fsocial.postservice.controller;

import com.fsocial.postservice.entity.MediaItem;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.services.UploadMedia;
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
    public MediaItem uploadFile(@RequestParam("fileUpload") MultipartFile file) throws AppCheckedException {
        MediaItem[] items = uploadImage.uploadMedia(new MultipartFile[]{file});
        return items[0];
    }

    @PostMapping("/upload-files")
    public List<MediaItem> uploadFiles(@RequestParam("fileUpload") MultipartFile[] file) throws AppCheckedException {
        MediaItem[] items = uploadImage.uploadMedia(file);
        return Arrays.asList(items);
    }
}
