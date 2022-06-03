package com.samithiwat.post.tag;

import com.github.javafaker.Faker;
import com.samithiwat.post.TestConfig;
import com.samithiwat.post.bloguser.entity.BlogUser;
import com.samithiwat.post.grpc.dto.BlogTag;
import com.samithiwat.post.grpc.dto.SortByType;
import com.samithiwat.post.grpc.tag.BlogTagListResponse;
import com.samithiwat.post.grpc.tag.BlogTagResponse;
import com.samithiwat.post.grpc.tag.FindAllTagRequest;
import com.samithiwat.post.post.entity.Post;
import com.samithiwat.post.tag.entity.Tag;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
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

    @InjectMocks
    BlogTagGrpcServiceImpl service;

    private Tag tag;
    private List<Tag> tags;
    private BlogTag tagDto;
    private List<BlogTag> tagsDto;
    private Post post;

    @BeforeEach
    void setup() {
        Faker faker = new Faker();

        BlogUser user = new BlogUser();
        user.setId(1L);
        user.setUserId(1L);

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

        this.tagDto = BlogTag.newBuilder()
                .setId(Math.toIntExact(this.tag.getId()))
                .setName(this.tag.getName())
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

        this.post = new Post(
                user,
                faker.lorem().word(),
                faker.lorem().sentence(),
                true,
                faker.date().past(1, TimeUnit.DAYS).toInstant()
        );
        this.post.setTags(this.tags);
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
        assertEquals(this.tagsDto, result.getDataList());
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
        assertEquals(this.tagsDto, result.getDataList());
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
        assertEquals(this.tagsDto, result.getDataList());
    }
}