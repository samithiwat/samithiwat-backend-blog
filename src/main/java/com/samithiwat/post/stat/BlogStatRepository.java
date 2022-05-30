package com.samithiwat.post.stat;

import com.samithiwat.post.stat.entity.BlogStat;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BlogStatRepository extends CrudRepository<BlogStat, Long> {
    @Modifying
    @Query("UPDATE BlogStat s SET s.likes = s.likes + 1 WHERE s.id = :id")
    boolean increaseLike(@Param("id") Long id);

    @Modifying
    @Query("UPDATE BlogStat s SET s.likes = s.likes - 1 WHERE s.id = :id")
    boolean decreaseLike(@Param("id") Long id);

    @Modifying
    @Query("UPDATE BlogStat s SET s.views = s.views + 1 WHERE s.id = :id")
    boolean increaseView(@Param("id") Long id);

    @Modifying
    @Query("UPDATE BlogStat s SET s.shares = s.shares + 1 WHERE s.id = :id")
    boolean increaseShare(@Param("id") Long id);
}
