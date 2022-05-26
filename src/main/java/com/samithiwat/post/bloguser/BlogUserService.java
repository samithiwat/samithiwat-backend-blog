package com.samithiwat.post.bloguser;

import com.samithiwat.post.grpc.dto.BlogUser;

public interface BlogUserService {
    BlogUser findOne(Long id);
}
