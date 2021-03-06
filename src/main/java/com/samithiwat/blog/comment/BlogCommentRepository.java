package com.samithiwat.blog.comment;

import com.samithiwat.blog.comment.entity.Comment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BlogCommentRepository extends CrudRepository<Comment,Long>{
    @Modifying
    @Query("UPDATE Comment c SET c.likes = c.likes + 1 WHERE c.id = :id")
    int increaseLike(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.likes = c.likes - 1 WHERE c.id = :id")
    int decreaseLike(@Param("id") Long id);
}
