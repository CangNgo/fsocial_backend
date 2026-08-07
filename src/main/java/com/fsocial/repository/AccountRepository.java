package com.fsocial.repository;

import com.fsocial.dto.ActorSnapshotDTO;
import com.fsocial.entity.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    @Query("select a from Account a left join fetch a.role where a.username = :username")
    Optional<Account> findByUsername(@Param("username") String username);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsernameOrEmail(String username, String email);
    long countByUsername(String username);
    long countByEmail(String email);
    long countByUsernameOrEmail(String username, String email);

    @Query(value = """
            select cast(extract(hour from created_at) as int) as h, count(*) as c
            from account where created_at between :startDate and :endDate
            group by 1 order by 1
            """, nativeQuery = true)
    List<Object[]> countByCreatedAtByHoursRaw(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            select to_char(created_at, 'YYYY-MM-DD') as d, count(*) as c
            from account where created_at between :startDate and :endDate
            group by 1 order by 1
            """, nativeQuery = true)
    List<Object[]> countByCreatedAtByDateRaw(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    default List<HourCountResult> countByCreatedAtByHours(LocalDateTime startDate, LocalDateTime endDate) {
        return countByCreatedAtByHoursRaw(startDate, endDate).stream()
                .map(r -> new HourCountResult(((Number) r[0]).intValue(), ((Number) r[1]).intValue()))
                .toList();
    }

    default List<DateCountResult> countByCreatedAtByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return countByCreatedAtByDateRaw(startDate, endDate).stream()
                .map(r -> new DateCountResult((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    record HourCountResult(Integer _id, Integer count) {}
    record DateCountResult(String _id, Long count) {}

    @Query("select new com.fsocial.postservice.dto.ActorSnapshotDTO(a.id, a.displayName, a.avatar) from Account a where a.id = :userId")
    Optional<ActorSnapshotDTO> findOwnerById(@Param("userId") String userId);

    /** Batch lookup — tránh N+1 khi build danh sách post. */
    @Query("select new com.fsocial.postservice.dto.ActorSnapshotDTO(a.id, a.displayName, a.avatar) from Account a where a.id in :userIds")
    List<ActorSnapshotDTO> findOwnersByIdIn(@Param("userIds") List<String> userIds);

    @Query("""
            select a from Account a
            where lower(a.displayName) like lower(concat('%', :keyword, '%'))
               or lower(a.firstName)   like lower(concat('%', :keyword, '%'))
               or lower(a.lastName)    like lower(concat('%', :keyword, '%'))
               or lower(a.email)       like lower(concat('%', :keyword, '%'))
            """)
    List<Account> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
