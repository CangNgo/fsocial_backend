package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, String> {

    @Modifying
    @Query(value = """
            insert into hashtag(name, usage_count, created_at, updated_at)
            select t as name, count(*) as usage_count, now(), now()
            from post, unnest(tags) as t
            where status = true
            group by t
            on conflict (name) do update set usage_count = excluded.usage_count, updated_at = now()
            """, nativeQuery = true)
    void syncCounts();

    @Modifying
    @Query(value = """
            delete from hashtag
            where name not in (select distinct unnest(tags) from post where status = true)
            """, nativeQuery = true)
    void deleteUnused();
}
