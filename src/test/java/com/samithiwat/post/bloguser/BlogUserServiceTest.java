package com.samithiwat.post.bloguser;


import com.github.javafaker.Faker;
import com.samithiwat.post.TestConfig;
import com.samithiwat.post.grpc.bloguser.BlogUserResponse;
import com.samithiwat.post.grpc.bloguser.BlogUserServiceGrpc;
import com.samithiwat.post.grpc.bloguser.FindOneUserRequest;
import com.samithiwat.post.grpc.dto.BlogUser;
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
public class BlogUserServiceTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Spy
    private BlogUserRepository repository;

    private BlogUser userDto;
    private Optional<com.samithiwat.post.bloguser.entity.BlogUser> user;
    private Faker faker;

    @BeforeEach
    void setup(){
        this.faker = new Faker();

        this.user = Optional.of(new com.samithiwat.post.bloguser.entity.BlogUser(1l));

        this.userDto = BlogUser.newBuilder()
                .setId(1)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();
    }

    @Test
    public void testFindOneSuccess(){
        BlogUserServiceGrpc.BlogUserServiceImplBase userService = new BlogUserServiceGrpc.BlogUserServiceImplBase() {
            @Override
            public void findOne(FindOneUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
                BlogUserResponse res = BlogUserResponse.newBuilder()
                        .setStatusCode(HttpStatus.OK.value())
                        .setData(userDto)
                        .build();

                responseObserver.onNext(res);
                responseObserver.onCompleted();
            }
        };

        try{
            grpcCleanup.register(InProcessServerBuilder.forName("user-service-test-findOne-success").directExecutor().addService(userService).build().start());
        }catch(Exception err){
            System.out.println("Error occurs while generating testing service");
            System.out.println(err.getMessage());
        }

        ManagedChannel chan = grpcCleanup.register(InProcessChannelBuilder.forName("user-service-test-findOne-success").directExecutor().build());
        BlogUserServiceGrpc.BlogUserServiceBlockingStub userBlockingStub = BlogUserServiceGrpc.newBlockingStub(chan);
        BlogUserServiceImpl service = new BlogUserServiceImpl(userBlockingStub);

        Assertions.assertEquals(this.userDto, service.findOne(1l));
    }

    @Test
    public void testFindOneNotFound(){
        BlogUserServiceGrpc.BlogUserServiceImplBase userService = new BlogUserServiceGrpc.BlogUserServiceImplBase() {
            @Override
            public void findOne(FindOneUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
                BlogUserResponse res = BlogUserResponse.newBuilder()
                        .setStatusCode(HttpStatus.NOT_FOUND.value())
                        .addErrors("Not found user")
                        .build();

                responseObserver.onNext(res);
                responseObserver.onCompleted();
            }
        };

        try{
            grpcCleanup.register(InProcessServerBuilder.forName("user-service-test-findOne-notfound").directExecutor().addService(userService).build().start());
        }catch(Exception err){
            System.out.println("Error occurs while generating testing service");
            System.out.println(err.getMessage());
        }

        ManagedChannel chan = grpcCleanup.register(InProcessChannelBuilder.forName("user-service-test-findOne-notfound").directExecutor().build());
        BlogUserServiceGrpc.BlogUserServiceBlockingStub userBlockingStub = BlogUserServiceGrpc.newBlockingStub(chan);
        BlogUserServiceImpl service = new BlogUserServiceImpl(userBlockingStub);

        Assertions.assertNull(service.findOne(1l));
    }

    @Test
    public void testFindOneOrCreateFounded(){
        Mockito.doReturn(this.user).when(this.repository).findByUserId(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());

        BlogUserServiceImpl service = new BlogUserServiceImpl(this.repository);

        com.samithiwat.post.bloguser.entity.BlogUser user = service.findOneOrCreate(1L);

        Assertions.assertEquals(this.user.get(), user);

        Mockito.verify(this.repository, Mockito.times(1)).findByUserId(1L);
        Mockito.verify(this.repository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    public void testFindOneOrCreateNotFound(){
        Mockito.doReturn(Optional.empty()).when(this.repository).findByUserId(1L);
        Mockito.doReturn(this.user.get()).when(this.repository).save(Mockito.any());

        BlogUserServiceImpl service = new BlogUserServiceImpl(this.repository);

        com.samithiwat.post.bloguser.entity.BlogUser user = service.findOneOrCreate(1L);

        Assertions.assertEquals(this.user.get(), user);

        Mockito.verify(this.repository, Mockito.times(1)).findByUserId(1L);
        Mockito.verify(this.repository, Mockito.times(1)).save(Mockito.any());
    }
}
