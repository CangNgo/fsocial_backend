package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.UserInterests;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInterestsRepository extends MongoRepository<UserInterests, String> {
    Optional<UserInterests> findByUserId(String userId);
    boolean existsByUserId(String userId);
    void deleteByUserId(String userId);
}
