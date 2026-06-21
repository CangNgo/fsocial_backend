package com.fsocial.postservice.repository;

import com.fsocial.postservice.dto.Account.OwnerDTO;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Owner;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsernameOrEmail(String username, String email);
    long countByUsername(String username);
    long countByEmail(String email);
    long countByUsernameOrEmail(String username, String email);

    @Aggregation(pipeline = {
        "{ '$match': { 'created_at': { '$gte': ?0, '$lte': ?1 } } }",
        "{ '$group': { '_id': { '$hour': '$created_at' }, 'count': { '$sum': 1 } } }"
    })
    List<HourCountResult> countByCreatedAtByHours(LocalDateTime startDate, LocalDateTime endDate);

    @Aggregation(pipeline = {
        "{ '$match': { 'created_at': { '$gte': ?0, '$lte': ?1 } } }",
        "{ '$group': { '_id': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$created_at' } }, 'count': { '$sum': 1 } } }",
        "{ '$sort': { '_id': 1 } }"
    })
    List<DateCountResult> countByCreatedAtByDate(LocalDateTime startDate, LocalDateTime endDate);

    record HourCountResult(Integer _id, Integer count) {}
    record DateCountResult(String _id, Long count) {}

    @Aggregation(pipeline = {
            "{'$match':  {'_id':  ?0}}"
    })
    Optional<OwnerDTO> findOwnerById(String userId);
}
