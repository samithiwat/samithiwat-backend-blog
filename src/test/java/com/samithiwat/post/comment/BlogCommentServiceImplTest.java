package com.samithiwat.post.comment;

import com.github.javafaker.Faker;
import com.samithiwat.post.TestConfig;
import com.samithiwat.post.bloguser.entity.BlogUser;
import com.samithiwat.post.comment.entity.Comment;
import com.samithiwat.post.grpc.blogcomment.BlogCommentListResponse;
import com.samithiwat.post.grpc.blogcomment.BlogCommentResponse;
import com.samithiwat.post.grpc.blogcomment.CreateCommentRequest;
import com.samithiwat.post.grpc.blogcomment.FindAllCommentByPostRequest;
import com.samithiwat.post.grpc.dto.BlogComment;
import com.samithiwat.post.post.BlogPostServiceImpl;
import com.samithiwat.post.post.entity.Post;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.userService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
class BlogCommentServiceImplTest {
    @Mock
    BlogCommentRepository repository;

    @Mock
    BlogPostServiceImpl postService;

    @InjectMocks
    BlogCommentServiceImpl service;

    private Comment comment;
    private List<Comment> comments;
    private BlogComment commentDto;
    private List<BlogComment> commentsDto;
    private Post post;

    @BeforeEach
    void setup(){
        Faker faker = new Faker();

        BlogUser user = new BlogUser();
        user.setId(1L);
        user.setUserId(1L);

        this.comment = new Comment(faker.lorem().paragraph());
        this.comment.setLikes(faker.random().nextLong(10000));
        this.comment.setCreatedDate(faker.date().past(10, TimeUnit.HOURS).toInstant());
        this.comment.setUpdatedDate(faker.date().past(5, TimeUnit.HOURS).toInstant());
        this.comment.setId(1L);

        Comment comment2 = new Comment(faker.lorem().paragraph());
        comment2.setLikes(faker.random().nextLong(10000));
        comment2.setCreatedDate(faker.date().past(10, TimeUnit.HOURS).toInstant());
        comment2.setUpdatedDate(faker.date().past(5, TimeUnit.HOURS).toInstant());
        comment2.setId(2L);

        Comment comment3 = new Comment(faker.lorem().paragraph());
        comment3.setLikes(faker.random().nextLong(10000));
        comment3.setCreatedDate(faker.date().past(10, TimeUnit.HOURS).toInstant());
        comment3.setUpdatedDate(faker.date().past(5, TimeUnit.HOURS).toInstant());
        comment3.setId(3L);

        this.comments = new ArrayList<>();
        this.comments.add(this.comment);
        this.comments.add(comment2);
        this.comments.add(comment3);

        this.commentDto = BlogComment.newBuilder()
                .setId(Math.toIntExact(this.comment.getId()))
                .setContent(this.comment.getContent())
                .setLikes(Math.toIntExact(this.comment.getLikes()))
                .setCreatedDate(this.comment.getCreatedDate().toString())
                .setUpdatedDate(this.comment.getUpdatedDate().toString())
                .build();

        BlogComment commentDto2 = BlogComment.newBuilder()
                .setId(Math.toIntExact(comment2.getId()))
                .setContent(comment2.getContent())
                .setLikes(Math.toIntExact(comment2.getLikes()))
                .setCreatedDate(comment2.getCreatedDate().toString())
                .setUpdatedDate(comment2.getUpdatedDate().toString())
                .build();

        BlogComment commentDto3 = BlogComment.newBuilder()
                .setId(Math.toIntExact(comment3.getId()))
                .setContent(comment3.getContent())
                .setLikes(Math.toIntExact(comment3.getLikes()))
                .setCreatedDate(comment3.getCreatedDate().toString())
                .setUpdatedDate(comment3.getUpdatedDate().toString())
                .build();

        this.commentsDto = new ArrayList<>();
        this.commentsDto.add(this.commentDto);
        this.commentsDto.add(commentDto2);
        this.commentsDto.add(commentDto3);

        this.post = new Post(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        this.post.setComments(this.comments);
    }

    @Test
    public void testFindAllCommentsFromPostSuccess() throws Exception{
        Mockito.doReturn(this.post).when(this.postService).findOneEntityBySlug(this.post.getSlug());

        FindAllCommentByPostRequest req = FindAllCommentByPostRequest.newBuilder()
                .setSlug(this.post.getSlug())
                .setLimit(10L)
                .setPage(1L)
                .build();

        StreamRecorder<BlogCommentListResponse> res = StreamRecorder.create();

        service.findAllCommentFromPost(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogCommentListResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogCommentListResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.commentsDto, result.getDataList());
    }

    @Test
    public void testFindAllCommentByPostNotFoundPost() throws Exception{
        Mockito.doReturn(null).when(this.postService).findOneEntityBySlug(this.post.getSlug());

        FindAllCommentByPostRequest req = FindAllCommentByPostRequest.newBuilder()
                .setSlug(this.post.getSlug())
                .setLimit(10L)
                .setPage(1L)
                .build();

        StreamRecorder<BlogCommentListResponse> res = StreamRecorder.create();

        service.findAllCommentFromPost(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogCommentListResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogCommentListResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(new ArrayList<>(), result.getDataList());
    }

    // TODO: Implement self reference

    @Test
    public void testCreateSuccess() throws Exception{
        Mockito.doReturn(this.post).when(this.postService).findOneEntityBySlug(this.post.getSlug());
        Mockito.doReturn(this.comment).when(this.repository).save(Mockito.any());

        CreateCommentRequest req = CreateCommentRequest.newBuilder()
                .setSlug(this.post.getSlug())
                .setUserId(Math.toIntExact(this.post.getAuthor().getId()))
                .setContent(this.comment.getContent())
                .build();

        StreamRecorder<BlogCommentResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogCommentResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogCommentResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.commentDto, result.getData());
    }

    @Test
    public void testCreateNotFoundPost() throws Exception{
        Mockito.doReturn(null).when(this.postService).findOneEntityBySlug(this.post.getSlug());
        Mockito.doReturn(this.comment).when(this.repository).save(Mockito.any());

        CreateCommentRequest req = CreateCommentRequest.newBuilder()
                .setSlug(this.post.getSlug())
                .setUserId(Math.toIntExact(this.post.getAuthor().getId()))
                .setContent(this.comment.getContent())
                .build();

        StreamRecorder<BlogCommentResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogCommentResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogCommentResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogComment.newBuilder().build(), result.getData());
    }
}