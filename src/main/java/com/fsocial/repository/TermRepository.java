package com.fsocial.repository;

import com.fsocial.entity.TermOfServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<TermOfServices, String> {
}
