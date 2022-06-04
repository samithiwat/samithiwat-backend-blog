package com.samithiwat.post.tag;

import com.samithiwat.post.tag.entity.Tag;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface BlogTagRepository extends CrudRepository<Tag, Long>, PagingAndSortingRepository<Tag, Long> {
    @Modifying
    @Query("UPDATE Tag t SET t.name = :name WHERE t.id = :id")
    boolean update(@Param("id") Long id, @Param("name") String name);
}
