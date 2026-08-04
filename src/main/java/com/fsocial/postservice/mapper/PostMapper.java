package com.fsocial.postservice.mapper;

import com.fsocial.postservice.dto.ActorSnapshotDTO;
import com.fsocial.postservice.dto.ContentDTO;
import com.fsocial.postservice.dto.post.MediaItemDTO;
import com.fsocial.postservice.dto.post.PostDTO;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Attachments;
import com.fsocial.postservice.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Content/ActorSnapshot embed đã bị bỏ: Post giờ có text/html phẳng, owner là FK sang Account,
 * media nằm ở bảng attachments. Mapper chỉ dựng DTO đọc ra — Post được build tay trong service
 * vì phải gắn quan hệ owner reference chứ không map thuần field.
 * {@code likes} trả rỗng ở đây; service nào cần danh sách thật thì set sau từ PostLikeRepository.
 */
@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "userId", source = "owner.id")
    @Mapping(target = "content", expression = "java(toContentDTO(post))")
    @Mapping(target = "likes", expression = "java(new java.util.ArrayList<String>())")
    PostDTO toPostDTO(Post post);

    ContentDTO toContentDTO(Post post);

    @Mapping(target = "userId", source = "id")
    ActorSnapshotDTO toActorSnapshot(Account account);

    @Mapping(target = "attachmentId", source = "id")
    @Mapping(target = "type", expression = "java(media.getMediaType() == null ? null : media.getMediaType().value())")
    MediaItemDTO toMediaItemDTO(Attachments media);
}
