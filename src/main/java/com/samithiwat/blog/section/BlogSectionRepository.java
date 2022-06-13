package com.samithiwat.blog.section;

import com.samithiwat.blog.section.entity.BlogSection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BlogSectionRepository extends CrudRepository<BlogSection, Long> {
    @Modifying
    @Query("UPDATE BlogSection s SET s.pos = :pos, s.content = :content WHERE s.id = :id")
    int update(@Param("id") int id, @Param("pos") int pos, @Param("content") String content);
}
