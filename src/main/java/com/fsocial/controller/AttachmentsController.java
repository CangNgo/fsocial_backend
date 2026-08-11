package com.fsocial.controller;

import com.fsocial.dto.ApiDataCursor;
import com.fsocial.dto.ApiResponse;
import com.fsocial.dto.Attachments.AttachementProfileDTO;
import com.fsocial.dto.Attachments.AttachmentDTO;
import com.fsocial.dto.page.AttachmentsRequest;
import com.fsocial.services.AttachmentsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/attachment")
public class AttachmentsController {

    AttachmentsService attachmentsService;

    @PostMapping
    public ApiResponse<ApiDataCursor<AttachmentDTO>> getAttachment(@RequestBody AttachmentsRequest pageRequest,
                                                                           @AuthenticationPrincipal Jwt jwt) {
        ApiDataCursor<AttachmentDTO> response =
                attachmentsService.getAttachmentByOwnerId(pageRequest, jwt.getSubject());
        return ApiResponse.<ApiDataCursor<AttachmentDTO>>builder()
                .data(response)
                .message("Get attachments successfully!")
                .build();
    }
}
