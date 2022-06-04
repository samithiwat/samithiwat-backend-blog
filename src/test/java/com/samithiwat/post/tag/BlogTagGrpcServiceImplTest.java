package com.samithiwat.post.tag;

import com.github.javafaker.Faker;
import com.samithiwat.post.TestConfig;
import com.samithiwat.post.bloguser.BlogUserServiceImpl;
import com.samithiwat.post.bloguser.entity.BUser;
import com.samithiwat.post.grpc.dto.*;
import com.samithiwat.post.grpc.dto.BlogTag;
import com.samithiwat.post.grpc.tag.*;
import com.samithiwat.post.post.entity.Post;
import com.samithiwat.post.stat.entity.BlogStat;
import com.samithiwat.post.tag.entity.Tag;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.blogStatService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
class BlogTagGrpcServiceImplTest {
    @Mock
    BlogTagRepository repository;

    @Mock
    BlogUserServiceImpl userService;

    @InjectMocks
    BlogTagGrpcServiceImpl service;

    private Tag tag;
    private List<Tag> tags;
    private BlogTag tagDto;
    private BlogTag tagDtoWithoutPost;
    private List<BlogTag> tagsDto;
    private List<BlogTag> tagsDtoWithoutPost;
    private Post post;
    private BlogUser userDto;
    private BUser user;

    @BeforeEach
    void setup() {
        Faker faker = new Faker();

        this.user = new BUser(1L);
        this.user.setId(1L);

        this.userDto = BlogUser.newBuilder()
                .setId(Math.toIntExact(this.user.getUserId()))
                .setDisplayName(faker.name().username())
                .setFirstname(faker.name().firstName())
                .setLastname(faker.name().lastName())
                .setDescription(faker.lorem().paragraph())
                .build();

        this.tag = new Tag(faker.lorem().word());
        this.tag.setId(1L);

        Tag tag2 = new Tag(faker.lorem().word());
        tag2.setId(2L);

        Tag tag3 = new Tag(faker.lorem().word());
        tag3.setId(3L);

        this.tags = new ArrayList<>();
        this.tags.add(this.tag);
        this.tags.add(tag2);
        this.tags.add(tag3);

        BlogStat stat = new BlogStat();
        stat.setLikes(faker.random().nextLong(100000));
        stat.setViews(faker.random().nextLong(100000));
        stat.setShares(faker.random().nextLong(100000));

        BlogStat stat2 = new BlogStat();
        stat2.setLikes(faker.random().nextLong(100000));
        stat2.setViews(faker.random().nextLong(100000));
        stat2.setShares(faker.random().nextLong(100000));

        BlogStat stat3 = new BlogStat();
        stat3.setLikes(faker.random().nextLong(100000));
        stat3.setViews(faker.random().nextLong(100000));
        stat3.setShares(faker.random().nextLong(100000));

        this.post = new Post(
                this.user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        this.post.setId(1L);
        this.post.setStat(stat);

        Post post2 = new Post(
                this.user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post2.setId(2L);
        post2.setStat(stat2);

        Post post3 = new Post(
                this.user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        post3.setId(3L);
        post3.setStat(stat3);

        List<Post> posts = new ArrayList<>();
        posts.add(this.post);
        posts.add(post2);
        posts.add(post3);

        BlogPostStat statDto = BlogPostStat.newBuilder()
                .setViews(Math.toIntExact(stat.getViews()))
                .setLikes(Math.toIntExact(stat.getLikes()))
                .setShares(Math.toIntExact(stat.getShares()))
                .build();

        BlogPostStat statDto2 = BlogPostStat.newBuilder()
                .setViews(Math.toIntExact(stat2.getViews()))
                .setLikes(Math.toIntExact(stat2.getLikes()))
                .setShares(Math.toIntExact(stat2.getShares()))
                .build();

        BlogPostStat statDto3 = BlogPostStat.newBuilder()
                .setViews(Math.toIntExact(stat3.getViews()))
                .setLikes(Math.toIntExact(stat3.getLikes()))
                .setShares(Math.toIntExact(stat3.getShares()))
                .build();

        BlogPost postDto = BlogPost.newBuilder()
                .setId(Math.toIntExact(this.post.getId()))
                .setSummary(this.post.getSummary())
                .setAuthor(this.userDto)
                .setStat(statDto)
                .setPublishDate(this.post.getPublishDate().toString())
                .build();

        BlogPost postDto2 = BlogPost.newBuilder()
                .setId(Math.toIntExact(post2.getId()))
                .setSummary(post2.getSummary())
                .setAuthor(this.userDto)
                .setStat(statDto2)
                .setPublishDate(post2.getPublishDate().toString())
                .build();

        BlogPost postDto3 = BlogPost.newBuilder()
                .setId(Math.toIntExact(post3.getId()))
                .setSummary(post3.getSummary())
                .setAuthor(this.userDto)
                .setStat(statDto3)
                .setPublishDate(post3.getPublishDate().toString())
                .build();

        List<BlogPost> postsDto = new ArrayList<>();
        postsDto.add(postDto);
        postsDto.add(postDto2);
        postsDto.add(postDto3);

        this.tagDto = BlogTag.newBuilder()
                .setId(Math.toIntExact(this.tag.getId()))
                .setName(this.tag.getName())
                .addAllPosts(postsDto)
                .build();

        BlogTag tagDto2 = BlogTag.newBuilder()
                .setId(Math.toIntExact(tag2.getId()))
                .setName(tag2.getName())
                .build();

        BlogTag tagDto3 = BlogTag.newBuilder()
                .setId(Math.toIntExact(tag3.getId()))
                .setName(tag3.getName())
                .build();

        this.tagsDto = new ArrayList<>();
        this.tagsDto.add(this.tagDto);
        this.tagsDto.add(tagDto2);
        this.tagsDto.add(tagDto3);

        this.tagDtoWithoutPost = BlogTag.newBuilder()
                .setId(Math.toIntExact(this.tag.getId()))
                .setName(this.tag.getName())
                .build();

        BlogTag tagDtoWithoutPost2 = BlogTag.newBuilder()
                .setId(Math.toIntExact(tag2.getId()))
                .setName(tag2.getName())
                .build();

        BlogTag tagDtoWithoutPost3 = BlogTag.newBuilder()
                .setId(Math.toIntExact(tag3.getId()))
                .setName(tag3.getName())
                .build();

        this.tagsDtoWithoutPost = new ArrayList<>();
        this.tagsDtoWithoutPost.add(this.tagDtoWithoutPost);
        this.tagsDtoWithoutPost.add(tagDtoWithoutPost2);
        this.tagsDtoWithoutPost.add(tagDtoWithoutPost3);

        this.tag.setPosts(posts);
    }

    @Test
    public void testFindAllAlphabetASCSuccess() throws Exception{
        Mockito.doReturn(this.tags).when(this.repository).findAll(Sort.by(Sort.Direction.ASC, "name"));

        FindAllTagRequest req = FindAllTagRequest.newBuilder()
                .setSortType(SortByType.ALPHABET_ASC)
                .build();

        StreamRecorder<BlogTagListResponse> res = StreamRecorder.create();

        service.findAll(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagListResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagListResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(this.tagsDtoWithoutPost, result.getDataList());
    }

    @Test
    public void testFindAllAlphabetDESCSuccess() throws Exception{
        Mockito.doReturn(this.tags).when(this.repository).findAll(Sort.by(Sort.Direction.DESC, "name"));

        FindAllTagRequest req = FindAllTagRequest.newBuilder()
                .setSortType(SortByType.ALPHABET_DESC)
                .build();

        StreamRecorder<BlogTagListResponse> res = StreamRecorder.create();

        service.findAll(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagListResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagListResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(this.tagsDtoWithoutPost, result.getDataList());
    }

    @Test
    public void testFindAllInvalidSortingType() throws Exception{
        Mockito.doReturn(this.tags).when(this.repository).findAll(Sort.by(Sort.Direction.ASC, "name"));

        FindAllTagRequest req = FindAllTagRequest.newBuilder()
                .setSortType(SortByType.ID_ASC)
                .build();

        StreamRecorder<BlogTagListResponse> res = StreamRecorder.create();

        service.findAll(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagListResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagListResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(this.tagsDtoWithoutPost, result.getDataList());
    }

    // FIX: GetPosts is null

    @Test
    public void testFindOneSuccess() throws Exception{
        Mockito.doReturn(Optional.of(this.tag)).when(this.repository).findById(this.tag.getId());
        Mockito.doReturn(this.userDto).when(this.userService).findOne(this.user.getUserId());

        FindOneTagRequest req = FindOneTagRequest.newBuilder()
                .setId(Math.toIntExact(this.tag.getId()))
                .build();

        StreamRecorder<BlogTagResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(this.tagDto, result.getData());
    }

    @Test
    public void testFindOneNotFound() throws Exception{
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(this.tag.getId());
        Mockito.doReturn(this.userDto).when(this.userService).findOne(this.user.getUserId());

        FindOneTagRequest req = FindOneTagRequest.newBuilder()
                .setId(Math.toIntExact(this.tag.getId()))
                .build();

        StreamRecorder<BlogTagResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogTag.newBuilder().build(), result.getData());
    }

    @Test
    public void testCreateSuccess() throws Exception{
        Mockito.doReturn(this.tag).when(this.repository).save(Mockito.any());

        CreateTagRequest req = CreateTagRequest.newBuilder()
                .setName(this.tag.getName())
                .build();

        StreamRecorder<BlogTagResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagResponse result = results.get(0);

        assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertEquals(this.tagDtoWithoutPost, result.getData());
    }

    @Test
    public void testCreateDuplicatedName() throws Exception{
        Mockito.doThrow(new DataIntegrityViolationException("Duplicated name")).when(this.repository).save(Mockito.any());

        CreateTagRequest req = CreateTagRequest.newBuilder()
                .setName(this.tag.getName())
                .build();

        StreamRecorder<BlogTagResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagResponse result = results.get(0);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertEquals(BlogTag.newBuilder().build(), result.getData());
    }

    @Test
    public void testUpdateSuccess() throws Exception{
        Mockito.doReturn(true).when(this.repository).update(this.tag.getId(), this.tag.getName());

        UpdateTagRequest req = UpdateTagRequest.newBuilder()
                .setId(Math.toIntExact(this.tag.getId()))
                .setName(this.tag.getName())
                .build();

        StreamRecorder<BlogTagStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagStatusResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagStatusResponse result = results.get(0);

        assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        assertEquals(0, result.getErrorsCount());
        assertTrue(result.getData());
    }

    @Test
    public void testUpdateNotFoundTag() throws Exception{
        Mockito.doReturn(false).when(this.repository).update(this.tag.getId(), this.tag.getName());

        UpdateTagRequest req = UpdateTagRequest.newBuilder()
                .setId(Math.toIntExact(this.tag.getId()))
                .setName(this.tag.getName())
                .build();

        StreamRecorder<BlogTagStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail();
        }

        List<BlogTagStatusResponse> results = res.getValues();

        assertEquals(1, results.size());

        BlogTagStatusResponse result = results.get(0);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        assertEquals(1, result.getErrorsCount());
        assertFalse(result.getData());
    }
}