package com.fsocial.controller;

import com.fsocial.services.AttachmentsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/attachments")
public class AttachmentsController {

    AttachmentsService attachmentsService;

//    @PostMapping
//    public ApiResponse<List<AttachmentDTO>> getAttachment(@RequestBody AttachmentsRequest pageRequest) throws AppCheckedException {
//        Pageable pageable =
//                PageRequest.of(pageRequest.getPage() - 1, pageRequest.getPageSize());
//        List<AttachmentDTO> attachments = attachmentsService.getAttachmentsByOwnerId(pageRequest.getUserId(), pageable);
//        return ApiResponse.<List<AttachmentDTO>>builder()
//                .data(attachments)
//                .message("Get attachments successfully!")
//                .build();
//    }
}
