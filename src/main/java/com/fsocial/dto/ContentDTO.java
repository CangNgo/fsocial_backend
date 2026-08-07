package com.fsocial.dto;

import com.fsocial.dto.post.MediaItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    String text;
    String html;
    List<MediaItemDTO> media;
}
