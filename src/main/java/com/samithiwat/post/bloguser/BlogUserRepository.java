package com.samithiwat.post.bloguser;

import com.samithiwat.post.bloguser.entity.BUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogUserRepository extends CrudRepository<BUser, Long> {
    @Query("SELECT u FROM BUser u WHERE u.userId = :id")
    Optional<BUser> findByUserId(@Param("id") Long userId);
}
