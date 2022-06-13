package com.samithiwat.blog.user;

import com.samithiwat.blog.grpc.dto.User;
import com.samithiwat.blog.grpc.user.FindOneUserRequest;
import com.samithiwat.blog.grpc.user.UserResponse;
import com.samithiwat.blog.grpc.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @GrpcClient("userservice")
    private final UserServiceGrpc.UserServiceBlockingStub userService;

    public UserServiceImpl(UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub) {
        this.userService = userServiceBlockingStub;
    }

    public User findOne(Long id){
        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(Math.toIntExact(id))
                .build();

        UserResponse res = userService.findOne(req);

        int statusCode = res.getStatusCode();

        if(statusCode != HttpStatus.OK.value()){
            return null;
        }

        return res.getData();
    }
}
