package com.samithiwat.blog.config;

import com.samithiwat.blog.grpc.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@GrpcClientBean(
        clazz = UserServiceGrpc.UserServiceBlockingStub.class,
        beanName = "UserServiceBlockingStub",
        client = @GrpcClient("UserService")
)
public class GrpcConfig {
}
