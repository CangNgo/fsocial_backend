package com.fsocial.postservice.mapper;

import com.fsocial.postservice.dto.ActorSnapshotDTO;
import com.fsocial.postservice.dto.post.PostDTO;
import com.fsocial.postservice.dto.post.PostDTORequest;
import com.fsocial.postservice.entity.ActorSnapshot;
import com.fsocial.postservice.entity.Content;
import com.fsocial.postservice.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {
    Post toPost(PostDTO postDTO);

    PostDTO toPostDTO(Post post);

    @Mapping(target = "content.text", source = "text")
    @Mapping(target = "content.media", ignore = true)
    Post toPost(PostDTORequest dto);

    ActorSnapshot toActorSnapshot(ActorSnapshotDTO actorSnapshotDTO);

    @Mapping(target = "media", ignore = true)
    void updateContent(PostDTORequest dto, @MappingTarget Content content);
}
