package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.TermOfServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<TermOfServices, String> {
}
