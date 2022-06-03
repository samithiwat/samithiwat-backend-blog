package com.samithiwat.post.comment;

import com.samithiwat.post.comment.entity.Comment;
import org.springframework.data.repository.CrudRepository;

public interface BlogCommentRepository extends CrudRepository<Comment,Long>{
}
