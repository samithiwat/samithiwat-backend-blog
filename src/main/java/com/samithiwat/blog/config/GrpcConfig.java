package com.samithiwat.blog.config;

import com.samithiwat.blog.grpc.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @GrpcClient("UserService")
    UserServiceGrpc.UserServiceBlockingStub userClient;

    @Bean
    UserServiceGrpc.UserServiceBlockingStub userClient(){
        return userClient;
    }

}
