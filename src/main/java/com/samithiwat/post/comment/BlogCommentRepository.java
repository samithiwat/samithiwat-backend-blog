package com.samithiwat.post.comment;

import com.samithiwat.post.comment.entity.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BlogCommentRepository extends CrudRepository<Comment,Long>, PagingAndSortingRepository<Comment, Long> {
}
