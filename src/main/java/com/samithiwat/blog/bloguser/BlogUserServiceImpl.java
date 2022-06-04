package com.samithiwat.blog.bloguser;

import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.grpc.bloguser.BlogUserResponse;
import com.samithiwat.blog.grpc.bloguser.BlogUserServiceGrpc;
import com.samithiwat.blog.grpc.bloguser.FindOneUserRequest;
import com.samithiwat.blog.grpc.dto.BlogUser;
import com.samithiwat.blog.grpc.dto.User;
import com.samithiwat.blog.user.UserServiceImpl;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BlogUserServiceImpl implements BlogUserService{

    @Autowired
    private UserServiceImpl userClient;

    @Autowired
    private BlogUserRepository repository;

    public BlogUserServiceImpl(UserServiceImpl client, BlogUserRepository repository) {
        this.userClient = client;
        this.repository = repository;
    }

    @Override
    public BlogUser findOne(Long id) {
        BUser user = this.repository.findById(id).orElse(null);

        if(user == null){
            return null;
        }

        User userDto = this.userClient.findOne(user.getUserId());

        if(userDto == null){
            return null;
        }

        return BlogUser.newBuilder()
                .setId(Math.toIntExact(user.getId()))
                .setDisplayName(userDto.getDisplayName())
                .setFirstname(userDto.getFirstname())
                .setLastname(userDto.getLastname())
                .setDescription(user.getDescription())
                .build();
    }

    @Override
    public BUser findOneEntity(Long id) {
        return this.repository.findById(id)
                .orElse(null);
    }
}
