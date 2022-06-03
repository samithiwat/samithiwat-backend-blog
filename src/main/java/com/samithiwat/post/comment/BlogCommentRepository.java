package com.samithiwat.post.comment;

import com.samithiwat.post.comment.entity.Comment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BlogCommentRepository extends CrudRepository<Comment,Long>{
    @Modifying
    @Query("UPDATE Comment c SET c.likes = c.likes + 1 WHERE c.id = :id")
    boolean increaseLike(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.likes = c.likes - 1 WHERE c.id = :id")
    boolean decreaseLike(@Param("id") Long id);
}
