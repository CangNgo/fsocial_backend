package com.fsocial.postservice.repository;

import com.fsocial.postservice.dto.Attachments.AttachmentDTO;
import com.fsocial.postservice.entity.Attachments;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentsRepository extends JpaRepository<Attachments, String> {

    @Query("""
            select new com.fsocial.postservice.dto.Attachments.AttachmentDTO(
                a.id, a.publicId, a.url, a.resourceType, a.fileType, a.size, a.ownerId, a.createdAt)
            from Attachments a where a.ownerId = :ownerId
            """)
    List<AttachmentDTO> getAttachmentsByOwnerId(@Param("ownerId") String ownerId, Pageable pageable);

    @Query("""
            select new com.fsocial.postservice.dto.Attachments.AttachmentDTO(
                a.id, a.publicId, a.url, a.resourceType, a.fileType, a.size, a.ownerId, a.createdAt)
            from Attachments a where a.fileType = :fileType
            """)
    List<AttachmentDTO> getAttachmentsByFileType(@Param("fileType") String fileType, Pageable pageable);

    @Modifying
    @Query("delete from Attachments a where a.ownerId = :postId")
    int deleteByOwnerId(@Param("postId") String postId);
}
