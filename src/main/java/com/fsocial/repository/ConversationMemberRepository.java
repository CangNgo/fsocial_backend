package com.fsocial.repository;

import com.fsocial.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, String> {

    Optional<ConversationMember> findByConversationIdAndUserId(String conversationId, String userId);

    boolean existsByConversationIdAndUserId(String conversationId, String userId);

    List<ConversationMember> findByConversationId(String conversationId);

    @Query("select m.conversation.id from ConversationMember m where m.userId = :userId")
    List<String> findConversationIdsByUserId(@Param("userId") String userId);

    @Query("select m.userId from ConversationMember m where m.conversation.id = :conversationId")
    List<String> findUserIdsByConversationId(@Param("conversationId") String conversationId);

    @Query("""
            select m.conversation.id from ConversationMember m
            where m.userId in :userIds
            group by m.conversation.id
            having count(distinct m.userId) = :memberCount
            """)
    List<String> findConversationIdsByExactMembers(@Param("userIds") List<String> userIds, @Param("memberCount") long memberCount);
}
