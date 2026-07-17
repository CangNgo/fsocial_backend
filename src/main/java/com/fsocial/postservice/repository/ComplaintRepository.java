package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Complaint;
import com.fsocial.postservice.enums.ComplaintType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
        Optional<Complaint> findByTargetIdAndComplaintType(String targetId, ComplaintType complaintType);

        // Methods from timelineService
        @org.springframework.data.mongodb.repository.Aggregation(pipeline = {
                        "{ '$unwind': '$details' }",
                        "{ '$match': { 'details.created_datetime': { '$gte': ?0, '$lte': ?1 } } }",
                        "{ '$group': { '_id': { '$hour': '$details.created_datetime' }, 'count': { '$sum': 1 } } }",
                        "{ '$project': { 'hour': '$_id', 'count': 1, '_id': 0 } }"
        })
        List<com.fsocial.postservice.dto.complaint.ComplaintStatisticsDTO> countByCreatedAtByHours(
                        java.time.LocalDateTime startDay, java.time.LocalDateTime endDay);

        @org.springframework.data.mongodb.repository.Aggregation(pipeline = {
                        "{ '$unwind': '$details' }",
                        "{ '$match': { 'details.created_datetime': { '$gte': ?0, '$lte': ?1 } } }",
                        "{ '$group': { '_id': { '$dateTrunc': { 'date': '$details.created_datetime', 'unit': 'day' } }, 'count': { '$sum': 1 } } }",
                        "{ '$project': { 'date': '$_id', 'count': 1, '_id': 0 } }",
                        "{ '$sort': { 'date': 1 } }"
        })
        List<com.fsocial.postservice.dto.complaint.ComplaintStatisticsLongDayDTO> countByDate(
                        java.time.LocalDateTime startDay, java.time.LocalDateTime endDay);
}
