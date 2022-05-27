package com.samithiwat.post.bloguser;

import com.samithiwat.post.bloguser.entity.BlogUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogUserRepository extends CrudRepository<BlogUser, Long> {
    @Query("SELECT u FROM BlogUser u WHERE u.userId = :id")
    Optional<BlogUser> findByUserId(@Param("id") Long userId);
}
