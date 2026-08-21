package com.fsocial.repository;

import com.fsocial.dto.faq.FaqRatingStatsDTO;
import com.fsocial.entity.FaqRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaqRatingRepository extends JpaRepository<FaqRating, FaqRating.Key> {

    Optional<FaqRating> findByFaqIdAndUserId(String faqId, String userId);

    @Query("select new com.fsocial.dto.faq.FaqRatingStatsDTO(coalesce(avg(r.score), 0.0), count(r)) " +
            "from FaqRating r where r.faqId = :faqId")
    FaqRatingStatsDTO getStats(@Param("faqId") String faqId);

    @Modifying
    @Query("delete from FaqRating r where r.faqId = :faqId")
    int deleteByFaqId(@Param("faqId") String faqId);
}
