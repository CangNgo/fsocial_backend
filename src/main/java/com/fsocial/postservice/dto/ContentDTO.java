package com.fsocial.postservice.dto;

import com.fsocial.postservice.entity.MediaItem;
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
    List<MediaItem> media;
}
