package com.samithiwat.post.post;

import com.samithiwat.post.post.entity.BlogPost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends CrudRepository<BlogPost, Long> {
    @Query("SELECT p FROM BlogPost p WHERE p.slug = :slug")
    Optional<BlogPost> findBySlug(@Param("slug") String slug);
}
