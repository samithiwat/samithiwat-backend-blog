package com.samithiwat.blog.user;

import com.samithiwat.blog.grpc.dto.User;

public interface UserService {
    User findOne(Long id);
}
