package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.TagCooccurrence;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagCooccurrenceRepository extends MongoRepository<TagCooccurrence, String> {

    List<TagCooccurrence> findByTagAOrderByCountDesc(String tagA, Pageable pageable);

    Optional<TagCooccurrence> findByTagAAndTagB(String tagA, String tagB);
}
