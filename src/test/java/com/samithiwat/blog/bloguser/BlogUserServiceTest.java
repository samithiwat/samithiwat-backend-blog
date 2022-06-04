package com.samithiwat.blog.bloguser;


import com.github.javafaker.Faker;
import com.samithiwat.blog.TestConfig;
import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.grpc.bloguser.BlogUserResponse;
import com.samithiwat.blog.grpc.bloguser.BlogUserServiceGrpc;
import com.samithiwat.blog.grpc.bloguser.FindOneUserRequest;
import com.samithiwat.blog.grpc.dto.BlogUser;
import com.samithiwat.blog.grpc.dto.User;
import com.samithiwat.blog.user.UserServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.userService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
public class BlogUserServiceTest{
    @Mock
    private BlogUserRepository repository;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private BlogUserServiceImpl service;

    private BlogUser blogUserDto;

    private User userDto;

    private BUser user;
    private Faker faker;

    @BeforeEach
    void setup(){
        this.faker = new Faker();

        this.user = new BUser(1L);
        this.user.setId(1L);
        this.user.setDescription(faker.lorem().paragraph());

        this.userDto = User.newBuilder()
                .setId(Math.toIntExact(this.user.getUserId()))
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        this.blogUserDto = BlogUser.newBuilder()
                .setId(Math.toIntExact(this.user.getId()))
                .setFirstname(this.userDto.getFirstname())
                .setLastname(this.userDto.getLastname())
                .setDisplayName(this.userDto.getDisplayName())
                .setDescription(this.user.getDescription())
                .build();
    }

    @Test
    public void testFindOneSuccess(){
        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(this.userDto).when(this.userService).findOne(this.user.getUserId());

        Assertions.assertEquals(this.blogUserDto, service.findOne(this.user.getId()));
    }

    @Test
    public void testFindOneNotFoundBUser(){
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(this.userDto).when(this.userService).findOne(this.user.getUserId());

        Assertions.assertNull(service.findOne(this.user.getId()));
    }

    @Test
    public void testFindOneNotFoundUserFromSamithiwatBackend(){
        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(null).when(this.userService).findOne(this.user.getUserId());

        Assertions.assertNull(service.findOneEntity(this.user.getId()));
    }

    @Test
    public void testFindOneEntitySuccess(){
        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findByUserId(1L);

        Assertions.assertEquals(this.user, service.findOneEntity(this.user.getId()));
    }

    @Test
    public void testFindOneEntityNotFound(){
        Mockito.doReturn(Optional.empty()).when(this.repository).findByUserId(1L);

        Assertions.assertNull(service.findOneEntity(this.user.getId()));
    }
}
