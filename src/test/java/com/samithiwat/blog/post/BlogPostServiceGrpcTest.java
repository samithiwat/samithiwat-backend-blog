package com.samithiwat.blog.post;

import com.github.javafaker.Faker;
import com.samithiwat.blog.TestConfig;
import com.samithiwat.blog.bloguser.BlogUserServiceImpl;
import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.grpc.post.*;
import com.samithiwat.blog.grpc.common.PaginationMetadata;
import com.samithiwat.blog.grpc.dto.BlogPost;
import com.samithiwat.blog.grpc.dto.BlogUser;
import com.samithiwat.blog.post.entity.Post;
import com.samithiwat.blog.stat.BlogStatGrpcServiceImpl;
import com.samithiwat.blog.stat.entity.BlogStat;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.userService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
public class BlogPostServiceGrpcTest {
    @Mock
    private BlogStatGrpcServiceImpl blogStatService;

    @Mock
    private BlogUserServiceImpl blogUserService;

    @Mock
    private BlogPostRepository repository;

    @InjectMocks
    private BlogPostServiceGrpcImpl service;

    private List<BlogPost> postDtos;
    private BlogPost postDto;
    private List<Post> posts;
    private Post post;
    private BlogUser userDto;
    private BUser user;
    private BlogStat stat;

    @BeforeEach
    void setup(){
        Faker faker = new Faker();

        this.stat = new BlogStat(1L);
        this.user = new BUser();
        user.setId(1L);
        user.setUserId(1L);

        this.userDto = BlogUser.newBuilder()
                .setId(1)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        this.posts = new ArrayList<>();
        this.post = new Post(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        this.post.setId(1L);

        Post post2 = new Post(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post2.setId(2L);

        Post post3 = new Post(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post3.setId(3L);
        this.posts.add(this.post);
        this.posts.add(post2);
        this.posts.add(post3);

        this.postDtos = new ArrayList<BlogPost>();
        this.postDto = BlogPost.newBuilder()
                .setId(1)
                .setAuthor(this.userDto)
                .setSlug(post.getSlug())
                .setSummary(post.getSummary())
                .setIsPublish(post.getPublished())
                .setPublishDate(post.getPublishDate().toString())
                .build();

        BlogPost postDto2 = BlogPost.newBuilder()
                .setId(2)
                .setAuthor(this.userDto)
                .setSlug(post2.getSlug())
                .setSummary(post2.getSummary())
                .setIsPublish(post2.getPublished())
                .setPublishDate(post2.getPublishDate().toString())
                .build();

        BlogPost postDto3 = BlogPost.newBuilder()
                .setId(3)
                .setAuthor(this.userDto)
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
    public void testFindAllWithPagination() throws Exception{
        Page<Post> postPagination = new Page<Post>() {
            @Override
            public int getTotalPages() {
                return 2;
            }

            @Override
            public long getTotalElements() {
                return 15;
            }

            @Override
            public <U> Page<U> map(Function<? super Post, ? extends U> converter) {
                return null;
            }

            @Override
            public int getNumber() {
                return 1;
            }

            @Override
            public int getSize() {
                return 5;
            }

            @Override
            public int getNumberOfElements() {
                return 10;
            }

            @Override
            public List<Post> getContent() {
                return posts;
            }

            @Override
            public boolean hasContent() {
                return false;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public Iterator<Post> iterator() {
                return null;
            }
        };

        PaginationMetadata metadata = PaginationMetadata.newBuilder()
                .setTotalItem(postPagination.getTotalElements())
                .setItemCount(postPagination.getNumberOfElements())
                .setItemsPerPage(postPagination.getSize())
                .setTotalPage(postPagination.getTotalPages())
                .setCurrentPage(postPagination.getNumber() + 1)
                .build();

        BlogPostPagination want = BlogPostPagination.newBuilder()
                .addItems(0, this.postDtos.get(0))
                .addItems(1, this.postDtos.get(1))
                .addItems(2, this.postDtos.get(2))
                .setMeta(metadata)
                .build();

        Mockito.doReturn(postPagination).when(this.repository).findAll(PageRequest.of(0, 10));
        Mockito.doReturn(this.userDto).when(this.blogUserService).findOne(1L);

        FindAllPostRequest req = FindAllPostRequest.newBuilder()
                .setLimit(10l)
                .setPage(1L)
                .build();

        StreamRecorder<BlogPostPaginationResponse> res = StreamRecorder.create();

        service.findAll(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostPaginationResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostPaginationResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(want, result.getData());
    }

    @Test
    public void testFindOneSuccess() throws Exception{
        Mockito.doReturn(Optional.of(this.post)).when(this.repository).findById(1L);
        Mockito.doReturn(this.userDto).when(this.blogUserService).findOne(1L);

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
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);
        Mockito.doReturn(this.userDto).when(this.blogUserService).findOne(1L);

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
        Mockito.doReturn(Optional.of(this.post)).when(this.repository).findById(1L);
        Mockito.doReturn(null).when(this.blogUserService).findOne(1L);

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
    public void testFindBySlug() throws Exception{
        Mockito.doReturn(Optional.of(this.post)).when(this.repository).findBySlug(this.postDto.getSlug());
        Mockito.doReturn(this.userDto).when(this.blogUserService).findOne(1L);

        FindBySlugPostRequest req = FindBySlugPostRequest.newBuilder()
                .setSlug(this.postDto.getSlug())
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.findBySlug(req, res);

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
    public void testFindBySlugNotFoundPost() throws Exception {
        Mockito.doReturn(Optional.empty()).when(this.repository).findBySlug(this.postDto.getSlug());
        Mockito.doReturn(this.userDto).when(this.blogUserService).findOne(1L);

        FindBySlugPostRequest req = FindBySlugPostRequest.newBuilder()
                .setSlug(this.postDto.getSlug())
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.findBySlug(req, res);

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
    public void testFindBySlugNotFoundUser() throws Exception {
        Mockito.doReturn(Optional.empty()).when(this.repository).findBySlug(this.postDto.getSlug());
        Mockito.doReturn(null).when(this.blogUserService).findOne(1L);

        FindBySlugPostRequest req = FindBySlugPostRequest.newBuilder()
                .setSlug(this.postDto.getSlug())
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.findBySlug(req, res);

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
    public void testCreateSuccess() throws Exception{
        Mockito.doReturn(this.stat).when(this.blogStatService).create(1L);
        Mockito.doReturn(this.userDto).when(this.blogUserService).findOne(1L);
        Mockito.doReturn(this.user).when(this.blogUserService).findOneEntity(1L);
        Mockito.doReturn(this.post).when(this.repository).save(Mockito.any());

        CreatePostRequest req = CreatePostRequest.newBuilder()
                .setAuthorId(this.postDto.getAuthor().getId())
                .setSlug(this.postDto.getSlug())
                .setSummary(this.postDto.getSummary())
                .setIsPublish(this.postDto.getIsPublish())
                .setPublishDate(this.postDto.getPublishDate())
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.postDto, result.getData());

        Mockito.verify(this.blogStatService, Mockito.times(1)).create(1L);
    }

    @Test
    public void testCreateNotFoundUser() throws Exception{
        Mockito.doReturn(this.stat).when(this.blogStatService).create(1L);
        Mockito.doReturn(null).when(this.blogUserService).findOne(1L);
        Mockito.doReturn(null).when(this.blogUserService).findOneEntity(1L);
        Mockito.doReturn(this.post).when(this.repository).save(Mockito.any());

        CreatePostRequest req = CreatePostRequest.newBuilder()
                .setAuthorId(this.postDto.getAuthor().getId())
                .setSlug(this.postDto.getSlug())
                .setSummary(this.postDto.getSummary())
                .setIsPublish(this.postDto.getIsPublish())
                .setPublishDate(this.postDto.getPublishDate())
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogPost.newBuilder().build(), result.getData());

        Mockito.verify(this.blogStatService, Mockito.times(0)).create(1L);
    }

    @Test
    public void testCreateDuplicatedSlug() throws Exception{
        Mockito.doReturn(this.stat).when(this.blogStatService).create(1L);
        Mockito.doReturn(this.userDto).when(this.blogUserService).findOne(1L);
        Mockito.doReturn(this.user).when(this.blogUserService).findOneEntity(1L);
        Mockito.doThrow(new DataIntegrityViolationException("Duplicated slug")).when(this.repository).save(Mockito.any());

        CreatePostRequest req = CreatePostRequest.newBuilder()
                .setAuthorId(this.postDto.getAuthor().getId())
                .setSlug(this.postDto.getSlug())
                .setSummary(this.postDto.getSummary())
                .setIsPublish(this.postDto.getIsPublish())
                .setPublishDate(this.postDto.getPublishDate())
                .build();

        StreamRecorder<BlogPostResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogPost.newBuilder().build(), result.getData());
        Mockito.verify(this.blogStatService, Mockito.times(0)).create(1L);
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        Mockito.doReturn(1).when(this.repository).update(1, this.postDto.getSlug(), this.postDto.getSummary(), this.postDto.getIsPublish(), Instant.parse(this.postDto.getPublishDate()));

        UpdatePostRequest req = UpdatePostRequest.newBuilder()
                .setId(1)
                .setSlug(this.postDto.getSlug())
                .setSummary(this.postDto.getSummary())
                .setIsPublish(this.postDto.getIsPublish())
                .setPublishDate(this.postDto.getPublishDate())
                .build();

        StreamRecorder<BlogPostStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testUpdateNotFoundPost() throws Exception {
        Mockito.doReturn(0).when(this.repository).update(1, this.postDto.getSlug(), this.postDto.getSummary(), this.postDto.getIsPublish(), Instant.parse(this.postDto.getPublishDate()));

        UpdatePostRequest req = UpdatePostRequest.newBuilder()
                .setId(1)
                .setSlug(this.postDto.getSlug())
                .setSummary(this.postDto.getSummary())
                .setIsPublish(this.postDto.getIsPublish())
                .setPublishDate(this.postDto.getPublishDate())
                .build();

        StreamRecorder<BlogPostStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testUpdateDuplicatedSlug() throws Exception{
        Mockito.doThrow(new DataIntegrityViolationException("Duplicated slug")).when(this.repository).update(1, this.postDto.getSlug(), this.postDto.getSummary(), this.postDto.getIsPublish(), Instant.parse(this.postDto.getPublishDate()));

        UpdatePostRequest req = UpdatePostRequest.newBuilder()
                .setId(1)
                .setSlug(this.postDto.getSlug())
                .setSummary(this.postDto.getSummary())
                .setIsPublish(this.postDto.getIsPublish())
                .setPublishDate(this.postDto.getPublishDate())
                .build();

        StreamRecorder<BlogPostStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        Mockito.doNothing().when(this.repository).deleteById(1L);

        DeletePostRequest req = DeletePostRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostStatusResponse> res = StreamRecorder.create();

        service.delete(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testDeleteNotFoundPost() throws Exception {
        Mockito.doThrow(new EmptyResultDataAccessException("Not found post", 1)).when(this.repository).deleteById(1L);

        DeletePostRequest req = DeletePostRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostStatusResponse> res = StreamRecorder.create();

        service.delete(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }
}
