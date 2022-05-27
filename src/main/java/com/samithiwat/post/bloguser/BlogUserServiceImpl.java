package com.samithiwat.post.bloguser;

import com.samithiwat.post.grpc.bloguser.BlogUserResponse;
import com.samithiwat.post.grpc.bloguser.BlogUserServiceGrpc;
import com.samithiwat.post.grpc.bloguser.FindOneUserRequest;
import com.samithiwat.post.grpc.dto.BlogUser;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BlogUserServiceImpl implements BlogUserService{

    @GrpcClient("BlogUserService")
    private BlogUserServiceGrpc.BlogUserServiceBlockingStub userClient;

    @Autowired
    private BlogUserRepository repository;

    public BlogUserServiceImpl() {}

    public BlogUserServiceImpl(BlogUserRepository repository) {
        this.repository = repository;
    }

    public BlogUserServiceImpl(BlogUserServiceGrpc.BlogUserServiceBlockingStub client) {
        this.userClient = client;
    }

    public BlogUserServiceImpl(BlogUserServiceGrpc.BlogUserServiceBlockingStub client, BlogUserRepository repository) {
        this.userClient = client;
        this.repository = repository;
    }

    @Override
    public BlogUser findOne(Long id) {
        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(Math.toIntExact(id))
                .build();

        BlogUserResponse res = userClient.findOne(req);


        int statusCode = res.getStatusCode();

        if (statusCode != HttpStatus.OK.value()){
            return null;
        }

        return res.getData();
    }

    @Override
    public com.samithiwat.post.bloguser.entity.BlogUser findOneEntityByUserId(Long id) {
        return this.repository.findByUserId(id).orElse(null);
    }
}
