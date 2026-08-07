package com.fsocial.repository;

import com.fsocial.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("delete from RefreshToken t where t.token = :token")
    int deleteByToken(@Param("token") String token);

    long countByUsername(String username);
    Optional<RefreshToken> findFirstByUsernameOrderByExpiryDateAsc(String username);
    Optional<RefreshToken> findFirstByUsernameOrderByExpiryDateDesc(String username);
}
