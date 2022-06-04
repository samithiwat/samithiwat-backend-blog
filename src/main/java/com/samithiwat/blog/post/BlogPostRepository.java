package com.samithiwat.blog.post;

import com.samithiwat.blog.post.entity.Post;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends CrudRepository<Post, Long>, PagingAndSortingRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.slug = :slug")
    Optional<Post> findBySlug(@Param("slug") String slug);

    @Modifying
    @Query("UPDATE Post p SET p.slug = :slug, p.summary = :summary, p.isPublished = :isPublished, p.publishDate = :publishDate WHERE p.id = :id")
    boolean update(@Param("id") int id, @Param("slug") String slug, @Param("summary") String summary, @Param("isPublish") boolean isPublish, @Param("publishDate") Instant publishDate);
}
