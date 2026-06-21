package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    long countByUsername(String username);
    Optional<RefreshToken> findFirstByUsernameOrderByExpiryDateAsc(String username);
    Optional<RefreshToken> findFirstByUsernameOrderByExpiryDateDesc(String username);
}
