package com.samithiwat.blog.bloguser;

import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.grpc.dto.BlogUser;

public interface BlogUserService {
    BlogUser findOne(Long id);
    BUser findOneEntity(Long id);
}
