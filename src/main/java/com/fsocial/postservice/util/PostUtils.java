package com.fsocial.postservice.util;

import com.fsocial.postservice.dto.post.ContentResponse;
import com.fsocial.postservice.dto.post.MediaResponse;
import com.fsocial.postservice.dto.post.PostResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Content;
import com.fsocial.postservice.entity.MediaItem;
import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.enums.MediaLayoutType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostUtils {
    public static PostResponse buildPostResponse(Post post, Account owner, int commentCount, String requesterId) {
        return PostResponse.builder()
                .id(post.getId())
                .originPostId(post.getOriginPostId())
                .content(buildContentResponse(post.getContent()))
                .countLikes(post.getLikes() == null ? 0 : post.getLikes().size())
                .countComments(commentCount)
                .userId(post.getOwner().getUserId())
                .displayName(DisplayNameUtils.build(owner))
                .avatar(owner.getAvatar())
                .createDatetime(post.getCreateDatetime())
                .isLike(post.getLikes() != null && post.getLikes().contains(requesterId))
                .isShare(Boolean.TRUE.equals(post.getIsShare()))
                .status(Boolean.TRUE.equals(post.getStatus()))
                .tags(post.getTags())
                .build();
    }

    public static ContentResponse buildContentResponse(Content content) {
        return ContentResponse.builder()
                .html(content.getHtml())
                .text(content.getText())
                .media(buildMediaResponse(content.getMedia()))
                .build();
    }

    public static List<MediaResponse> buildMediaResponse(List<MediaItem> mediaItem) {
        if (mediaItem == null || mediaItem.isEmpty()) {
            return new ArrayList<>();
        }
        return mediaItem.stream()
                .filter(Objects::nonNull)
                .map(item -> {
            double ratio = (item.getWidth() == null || item.getHeight() == null || item.getHeight() == 0)
                    ? 1.0
                    : (double) item.getWidth() / item.getHeight();
            log.info("Ratio: {}", ratio);
            MediaLayoutType layoutType = calculateLayoutType(ratio);
            return MediaResponse.builder()
                    .type(item.getType())
                    .url(item.getUrl())
                    .width(item.getWidth())
                    .height(item.getHeight())
                    .ratio(ratio)
                    .mediaType(layoutType)
                    .build();
        }).toList();
    }

    public static MediaLayoutType calculateLayoutType(double ratio) {
        if (ratio > 2.0) {
            return MediaLayoutType.PANORAMA;
        } else if (ratio > 1.2) {
            return MediaLayoutType.LANDSCAPE;
        } else if (ratio > 0.9) {
            return MediaLayoutType.SQUARE;
        } else {
            return MediaLayoutType.PORTRAIT;
        }
    }
}
