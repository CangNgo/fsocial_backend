package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Relationship;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelationshipRepository extends MongoRepository<Relationship, String> {
    Optional<Relationship> findByUserId(String userId);
}
