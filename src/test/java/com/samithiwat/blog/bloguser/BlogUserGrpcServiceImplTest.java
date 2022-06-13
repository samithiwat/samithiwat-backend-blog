package com.samithiwat.blog.bloguser;

import com.github.javafaker.Faker;
import com.samithiwat.blog.TestConfig;
import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.grpc.bloguser.*;
import com.samithiwat.blog.grpc.dto.BlogUser;
import com.samithiwat.blog.grpc.dto.User;
import com.samithiwat.blog.post.BlogPostServiceImpl;
import com.samithiwat.blog.post.entity.Post;
import com.samithiwat.blog.user.UserServiceImpl;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-bloguser", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.blogUserService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@ExtendWith(SpringExtension.class)
class BlogUserGrpcServiceImplTest {
    @Mock
    private UserServiceImpl userService;

    @Mock
    private BlogPostServiceImpl postService;

    @Mock
    private BlogUserRepository repository;

    @InjectMocks
    private BlogUserGrpcServiceImpl service;

    private BUser user;
    private User userDto;
    private BlogUser blogUserDto;
    private Post post;

    @BeforeEach
    void setup(){
        Faker faker = new Faker();

        this.user = new BUser();
        this.user.setId(1L);
        this.user.setDescription(faker.lorem().sentence());
        this.user.setUserId(1L);

        List<Post> posts = new ArrayList<Post>();
        this.post = new Post(
                this.user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        this.post.setId(1L);

        Post post1 = new Post(
                this.user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post1.setId(1L);

        Post post2 = new Post(
                this.user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post2.setId(2L);

        Post post3 = new Post(
                this.user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post3.setId(3L);

        posts.add(post1);
        posts.add(post2);
        posts.add(post3);

        this.user.setBookmarks(posts);
        this.user.setReads(posts);

        this.userDto = User.newBuilder()
                .setId(1)
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDisplayName(faker.name().username())
                .build();

        this.blogUserDto = BlogUser.newBuilder()
                .setId(Math.toIntExact(this.user.getId()))
                .setFirstname(this.userDto.getFirstname())
                .setLastname(this.userDto.getLastname())
                .setDisplayName(this.userDto.getDisplayName())
                .setImageUrl(this.userDto.getImageUrl())
                .setDescription(this.user.getDescription())
                .build();
    }

    @Test
    public void testFindOneSuccess() throws Exception {
        Mockito.doReturn(userDto).when(this.userService).findOne(this.user.getUserId());
        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(this.user.getId());

        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(this.blogUserDto, result.getData());
    }

    @Test
    public void testFindOneNotFoundInDatabase() throws Exception {
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(userDto).when(this.userService).findOne(this.user.getUserId());

        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testFindOneInvalidUserID() throws Exception {
        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(null).when(this.userService).findOne(this.user.getUserId());

        FindOneUserRequest req = FindOneUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testCreateSuccess() throws Exception {
        Mockito.doReturn(this.userDto).when(this.userService).findOne(this.user.getUserId());
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());

        CreateUserRequest req = CreateUserRequest.newBuilder()
                .setUserId(Math.toIntExact(user.getUserId()))
                .setDescription(user.getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(this.blogUserDto, result.getData());
    }

    @Test
    public void testCreateUserDuplicated() throws Exception{
        Mockito.doReturn(this.userDto).when(this.userService).findOne(this.user.getUserId());
        Mockito.doThrow(new DataIntegrityViolationException("Duplicated userId")).when(this.repository).save(Mockito.any());

        CreateUserRequest req = CreateUserRequest.newBuilder()
                .setUserId(Math.toIntExact(this.user.getUserId()))
                .setDescription(user.getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testCreateUserNotFound() throws Exception {
        Mockito.doReturn(null).when(this.userService).findOne(this.user.getUserId());
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());

        CreateUserRequest req = CreateUserRequest.newBuilder()
                .setUserId(Math.toIntExact(this.user.getUserId()))
                .setDescription(user.getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        BlogUser want = BlogUser.newBuilder()
                .setId(Math.toIntExact(this.user.getUserId()))
                .setDescription(this.user.getDescription())
                .build();

        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(user).when(this.repository).save(user);

        UpdateUserRequest req = UpdateUserRequest.newBuilder()
                .setId(1)
                .setDescription(user.getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(want, result.getData());
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(user).when(this.repository).save(user);

        UpdateUserRequest req = UpdateUserRequest.newBuilder()
                .setId(1)
                .setDescription(user.getDescription())
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        Mockito.doNothing().when(this.repository).deleteById(this.user.getId());

        DeleteUserRequest req = DeleteUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.delete(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.NO_CONTENT.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        Mockito.doThrow(new EmptyResultDataAccessException("Not found user", 1)).when(this.repository).deleteById(this.user.getId());

        DeleteUserRequest req = DeleteUserRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogUserResponse> res = StreamRecorder.create();

        service.delete(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BlogUserResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogUserResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogUser.newBuilder().build(), result.getData());
    }

    @Test
    public void testAddBookmarkFounded() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);
        want.add(3);
        want.add(4);

        this.post.setId(4L);

        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneEntityById(this.post.getId());

        AddBookmarkRequest req = AddBookmarkRequest.newBuilder()
                .setUserId(Math.toIntExact(this.user.getId()))
                .setPostId(Math.toIntExact(this.post.getId()))
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.addBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BookmarkResponse> results = res.getValues();

        assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(want, result.getDataList());
    }

    @Test
    public void testAddBookmarkNotFound() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);
        want.add(3);
        want.add(4);

        this.post.setId(4L);

        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(this.user.getId());
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneEntityById(this.post.getId());

        AddBookmarkRequest req = AddBookmarkRequest.newBuilder()
                .setUserId(Math.toIntExact(this.user.getId()))
                .setPostId(Math.toIntExact(this.post.getId()))
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.addBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BookmarkResponse> results = res.getValues();

        assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(want, result.getDataList());
    }

    @Test
    public void testAddBookmarkNotFoundUser() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneEntityById(1L);

        AddBookmarkRequest req = AddBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(1)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.addBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BookmarkResponse> results = res.getValues();

        assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(new ArrayList<Integer>(), result.getDataList());
    }

    @Test
    public void testFindAllBookmarkSuccess() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);
        want.add(3);

        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(1L);

        FindAllBookmarkRequest req = FindAllBookmarkRequest.newBuilder()
                .setUserId(1)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.findAllBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BookmarkResponse> results = res.getValues();

        assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(want, result.getDataList());
    }

    @Test
    public void testFindAllBookmarkUserNotFound() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);

        FindAllBookmarkRequest req = FindAllBookmarkRequest.newBuilder()
                .setUserId(1)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.findAllBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BookmarkResponse> results = res.getValues();

        assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(new ArrayList<Integer>(), result.getDataList());
    }

    @Test
    public void testDeleteBookmarkSuccess() throws Exception{
        List<Integer> want = new ArrayList<Integer>();
        want.add(1);
        want.add(2);

        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(1L);

        DeleteBookmarkRequest req = DeleteBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.deleteBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BookmarkResponse> results = res.getValues();

        assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(want, result.getDataList());
    }

    @Test
    public void testDeleteBookmarkUserNotFound() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);

        DeleteBookmarkRequest req = DeleteBookmarkRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<BookmarkResponse> res = StreamRecorder.create();

        service.deleteBookmark(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<BookmarkResponse> results = res.getValues();

        assertEquals(1, results.size());

        BookmarkResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(new ArrayList<Integer>(), result.getDataList());
    }

    @Test
    public void testReadPostSuccess() throws Exception{
        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(1L);
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneEntityById(3L);

        ReadRequest req = ReadRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<ReadResponse> res = StreamRecorder.create();

        service.read(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<ReadResponse> results = res.getValues();

        assertEquals(1, results.size());

        ReadResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertTrue(result.getData());
    }

    @Test
    public void testReadPostNotFoundUser() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneEntityById(3L);

        ReadRequest req = ReadRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<ReadResponse> res = StreamRecorder.create();

        service.read(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<ReadResponse> results = res.getValues();

        assertEquals(1, results.size());

        ReadResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertFalse(result.getData());
    }

    @Test
    public void testReadPostNotFoundPost() throws Exception{
        Mockito.doReturn(Optional.of(this.user)).when(this.repository).findById(1L);
        Mockito.doReturn(this.user).when(this.repository).save(Mockito.any());
        Mockito.doReturn(this.post).when(this.postService).findOneEntityById(3L);

        ReadRequest req = ReadRequest.newBuilder()
                .setUserId(1)
                .setPostId(3)
                .build();

        StreamRecorder<ReadResponse> res = StreamRecorder.create();

        service.read(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            fail();
        }

        List<ReadResponse> results = res.getValues();

        assertEquals(1, results.size());

        ReadResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertTrue(result.getData());
    }
}