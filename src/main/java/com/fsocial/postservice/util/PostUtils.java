package com.fsocial.postservice.util;

import com.fsocial.postservice.dto.post.ContentResponse;
import com.fsocial.postservice.dto.post.MediaResponse;
import com.fsocial.postservice.dto.post.PostResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Attachments;
import com.fsocial.postservice.entity.Post;
import com.fsocial.postservice.enums.MediaLayoutType;
import com.fsocial.postservice.enums.MediaType;
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

    /** likeCount / isLike vẫn ở bảng riêng (post_like) nên phải truyền vào; tags đọc thẳng từ entity. */
    public static PostResponse buildPostResponse(Post post, Account owner, int commentCount,
                                                 int likeCount, boolean liked) {
        return PostResponse.builder()
                .id(post.getId())
                .originPostId(post.getOriginPostId())
                .content(buildContentResponse(post))
                .countLikes(likeCount)
                .countComments(commentCount)
                .userId(post.getOwner().getId())
                .displayName(DisplayNameUtils.build(owner))
                .avatar(owner == null ? null : owner.getAvatar())
                .createDatetime(post.getCreateDatetime())
                .isLike(liked)
                .isShare(Boolean.TRUE.equals(post.getIsShare()))
                .status(Boolean.TRUE.equals(post.getStatus()))
                .tags(post.getTags() == null ? List.of() : post.getTags())
                .build();
    }

    public static ContentResponse buildContentResponse(Post post) {
        return ContentResponse.builder()
                .html(post.getHtml())
                .text(post.getText())
                .media(buildMediaResponse(post.getMedia()))
                .build();
    }

    public static List<MediaResponse> buildMediaResponse(List<Attachments> mediaItems) {
        if (mediaItems == null || mediaItems.isEmpty()) {
            return new ArrayList<>();
        }
        return mediaItems.stream()
                .filter(Objects::nonNull)
                .map(item -> toMediaResponse(item.getMediaType(), item.getUrl(), item.getWidth(), item.getHeight()))
                .toList();
    }

    /** Dùng chung cho attachments và comment_media — hai entity khác nhau nhưng cùng bộ field. */
    public static MediaResponse toMediaResponse(MediaType type, String url, Integer width, Integer height) {
        double ratio = (width == null || height == null || height == 0)
                ? 1.0
                : (double) width / height;
        return MediaResponse.builder()
                .type(type == null ? null : type.value())
                .url(url)
                .width(width)
                .height(height)
                .ratio(ratio)
                .mediaType(calculateLayoutType(ratio))
                .build();
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
