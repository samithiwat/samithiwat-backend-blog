package com.samithiwat.post.post;

import com.github.javafaker.Faker;
import com.samithiwat.post.TestConfig;
import com.samithiwat.post.bloguser.BlogUserServiceImpl;
import com.samithiwat.post.grpc.blogpost.BlogPostResponse;
import com.samithiwat.post.grpc.blogpost.FindOnePostRequest;
import com.samithiwat.post.grpc.dto.BlogPost;
import com.samithiwat.post.grpc.dto.BlogUser;
import io.grpc.internal.testing.StreamRecorder;
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

import java.util.ArrayList;
import java.util.List;
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
    private BlogUserServiceImpl blogUserService;

    @Spy
    private BlogPostRepository repository;

    private List<BlogPost> postDtos;
    private BlogPost postDto;
    private List<com.samithiwat.post.post.entity.BlogPost> posts;
    private Optional<com.samithiwat.post.post.entity.BlogPost> post;
    private BlogUser user;
    private Faker faker;

    @BeforeEach
    void setup(){
        this.faker = new Faker();

        com.samithiwat.post.bloguser.entity.BlogUser user = new com.samithiwat.post.bloguser.entity.BlogUser();
        user.setId(1l);
        user.setUserId(1l);

        this.user = BlogUser.newBuilder()
                .setId(1)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        this.posts = new ArrayList<>();
        this.post = Optional.of(new com.samithiwat.post.post.entity.BlogPost(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        ));
        this.post.get().setId(1l);

        com.samithiwat.post.post.entity.BlogPost post2 = new com.samithiwat.post.post.entity.BlogPost(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post2.setId(2l);

        com.samithiwat.post.post.entity.BlogPost post3 = new com.samithiwat.post.post.entity.BlogPost(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post3.setId(3l);
        this.posts.add(this.post.get());
        this.posts.add(post2);
        this.posts.add(post3);

        this.postDtos = new ArrayList<BlogPost>();
        this.postDto = BlogPost.newBuilder()
                .setId(1)
                .setAuthor(this.user)
                .setSlug(post.get().getSlug())
                .setSummary(post.get().getSummary())
                .setIsPublish(post.get().getPublished())
                .setPublishDate(post.get().getPublishDate().toString())
                .build();

        BlogPost postDto2 = BlogPost.newBuilder()
                .setId(2)
                .setAuthor(this.user)
                .setSlug(post2.getSlug())
                .setSummary(post2.getSummary())
                .setIsPublish(post2.getPublished())
                .setPublishDate(post2.getPublishDate().toString())
                .build();

        BlogPost postDto3 = BlogPost.newBuilder()
                .setId(3)
                .setAuthor(this.user)
                .setSlug(post3.getSlug())
                .setSummary(post3.getSummary())
                .setIsPublish(post3.getPublished())
                .setPublishDate(post3.getPublishDate().toString())
                .build();

        this.postDtos.add(this.postDto);
        this.postDtos.add(postDto2);
        this.postDtos.add(postDto3);
    }

    @Test
    public void testFindOneSuccess() throws Exception{
        Mockito.doReturn(this.post).when(this.repository).findById(1l);
        Mockito.doReturn(this.user).when(this.blogUserService).findOne(1l);

        BlogPostServiceImpl service = new BlogPostServiceImpl(this.repository, this.blogUserService);

        FindOnePostRequest req = FindOnePostRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.postDto, result.getData());
    }

    @Test
    public void testFindOneNotFoundPost() throws Exception {
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1l);
        Mockito.doReturn(this.user).when(this.blogUserService).findOne(1l);

        BlogPostServiceImpl service = new BlogPostServiceImpl(this.repository, this.blogUserService);

        FindOnePostRequest req = FindOnePostRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogPost.newBuilder().build(), result.getData());
    }

    @Test
    public void testFindOneNotFoundUser() throws Exception {
        Mockito.doReturn(this.post).when(this.repository).findById(1l);
        Mockito.doReturn(null).when(this.blogUserService).findOne(1l);

        BlogPostServiceImpl service = new BlogPostServiceImpl(this.repository, this.blogUserService);

        FindOnePostRequest req = FindOnePostRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogPost.newBuilder().build(), result.getData());
    }

}
