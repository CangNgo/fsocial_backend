package com.fsocial.repository;

import com.fsocial.entity.Faq;
import com.fsocial.enums.FaqType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, String> {
    List<Faq> findByType(FaqType type);
}
