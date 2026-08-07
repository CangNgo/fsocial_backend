package com.fsocial.repository;

import com.fsocial.dto.complaint.ComplaintStatisticsDTO;
import com.fsocial.dto.complaint.ComplaintStatisticsLongDayDTO;
import com.fsocial.entity.Complaint;
import com.fsocial.enums.ComplaintType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, String> {

    Optional<Complaint> findByTargetIdAndComplaintType(String targetId, ComplaintType complaintType);

    /** Thay {@code $unwind: '$details'} — đếm trực tiếp trên bảng complaint_detail. */
    @Query(value = """
            select cast(extract(hour from create_datetime) as int) as h, count(*) as c
            from complaint_detail
            where create_datetime between :startDay and :endDay
            group by 1 order by 1
            """, nativeQuery = true)
    List<Object[]> countByCreatedAtByHoursRaw(@Param("startDay") LocalDateTime startDay,
                                              @Param("endDay") LocalDateTime endDay);

    @Query(value = """
            select date_trunc('day', create_datetime) as d, count(*) as c
            from complaint_detail
            where create_datetime between :startDay and :endDay
            group by 1 order by 1
            """, nativeQuery = true)
    List<Object[]> countByDateRaw(@Param("startDay") LocalDateTime startDay,
                                  @Param("endDay") LocalDateTime endDay);

    default List<ComplaintStatisticsDTO> countByCreatedAtByHours(LocalDateTime startDay, LocalDateTime endDay) {
        return countByCreatedAtByHoursRaw(startDay, endDay).stream()
                .map(r -> ComplaintStatisticsDTO.builder()
                        .hour(String.valueOf(((Number) r[0]).intValue()))
                        .count(((Number) r[1]).intValue())
                        .build())
                .toList();
    }

    default List<ComplaintStatisticsLongDayDTO> countByDate(LocalDateTime startDay, LocalDateTime endDay) {
        return countByDateRaw(startDay, endDay).stream()
                .map(r -> ComplaintStatisticsLongDayDTO.builder()
                        .date(((Timestamp) r[0]).toLocalDateTime())
                        .count(((Number) r[1]).intValue())
                        .build())
                .toList();
    }
}
