package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    Optional<Token> findByToken(String token);
    Optional<Token> findByAccount(Account account);
}
