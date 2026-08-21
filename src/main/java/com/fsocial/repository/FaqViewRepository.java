package com.fsocial.repository;

import com.fsocial.entity.FaqView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqViewRepository extends JpaRepository<FaqView, String> {

    long countByFaqId(String faqId);

    @Query("select count(distinct v.userId) from FaqView v where v.faqId = :faqId")
    long countDistinctUserIdByFaqId(@Param("faqId") String faqId);

    @Modifying
    @Query("delete from FaqView v where v.faqId = :faqId")
    int deleteByFaqId(@Param("faqId") String faqId);
}
