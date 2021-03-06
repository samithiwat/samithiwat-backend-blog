package com.samithiwat.blog.post;

import com.github.javafaker.Faker;
import com.samithiwat.blog.TestConfig;
import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.post.entity.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.userService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
public class BlogPostServiceTest {
    @Spy
    BlogPostRepository repository;

    @InjectMocks
    BlogPostServiceImpl service;

    private Post post;

    @BeforeEach
    void setup(){
        Faker faker = new Faker();

        BUser user = new BUser();
        user.setId(1L);
        user.setUserId(1L);

        this.post = new Post(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
    }

    @Test
    public void testFindOneEntityByIdSuccess(){
        Mockito.doReturn(Optional.of(this.post)).when(this.repository).findById(1L);

        Post result = this.service.findOneEntityById(1L);

        Assertions.assertEquals(this.post, result);
    }

    @Test
    public void testFindOneEntityByIdNotFound(){
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);

        Post result = this.service.findOneEntityById(1L);

        Assertions.assertEquals(null, result);
    }

    @Test
    public void testFindOneEntityBySlugSuccess(){
        Mockito.doReturn(Optional.of(this.post)).when(this.repository).findBySlug(this.post.getSlug());

        Post result = this.service.findOneEntityBySlug(this.post.getSlug());

        Assertions.assertEquals(this.post, result);
    }

    @Test
    public void testFindOneEntityBySlugNotFound(){
        Mockito.doReturn(Optional.empty()).when(this.repository).findBySlug(this.post.getSlug());

        Post result = this.service.findOneEntityBySlug(this.post.getSlug());

        Assertions.assertEquals(null, result);
    }


}
