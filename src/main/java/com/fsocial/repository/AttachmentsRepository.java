package com.fsocial.repository;

import com.fsocial.dto.Attachments.AttachementProfileDTO;
import com.fsocial.dto.Attachments.AttachmentDTO;
import com.fsocial.entity.Attachments;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentsRepository extends JpaRepository<Attachments, String> {

    List<AttachmentDTO> findByFileType(String fileType, Pageable pageable);

    @Modifying
    @Query("delete from Attachments a where a.ownerId = :postId")
    int deleteByOwnerId(@Param("postId") String postId);

    Window<AttachementProfileDTO> findByOwnerIdAndResourceType(String userId, String resourceType, Sort sort, ScrollPosition position);
}
