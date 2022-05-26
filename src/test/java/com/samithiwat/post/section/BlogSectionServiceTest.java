package com.samithiwat.post.section;

import com.github.javafaker.Faker;
import com.samithiwat.post.TestConfig;
import com.samithiwat.post.common.ContentType;
import com.samithiwat.post.grpc.blogsection.BlogPostSectionResponse;
import com.samithiwat.post.grpc.blogsection.CreatePostSectionRequest;
import com.samithiwat.post.grpc.blogsection.FindOnePostSectionRequest;
import com.samithiwat.post.grpc.dto.BlogPostSection;
import com.samithiwat.post.grpc.dto.PostContentType;
import com.samithiwat.post.section.entity.BlogSection;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
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
        "grpc.client.blogSectionService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
public class BlogSectionServiceTest {
    @Spy
    private BlogSectionRepository repository;

    @InjectMocks
    private BlogSectionServiceImpl service;

    private List<BlogPostSection> sectionDtos;
    private BlogPostSection sectionDto;
    private List<BlogSection> sections;
    private Optional<BlogSection> section;
    private Faker faker;

    @BeforeEach
    void setup(){
        this.faker = new Faker();

        this.sections = new ArrayList<>();
        this.section = Optional.of(new BlogSection(1, ContentType.IMAGE, faker.internet().image()));
        this.section.get().setId(1l);

        BlogSection section2 = new BlogSection(2, ContentType.TEXT, faker.lorem().paragraph());
        section2.setId(2l);

        BlogSection section3 = new BlogSection(3, ContentType.CODE, faker.lorem().paragraph());
        section2.setId(3l);

        this.sections.add(this.section.get());
        this.sections.add(section2);
        this.sections.add(section3);

        this.sectionDtos = new ArrayList<BlogPostSection>();
        this.sectionDto = BlogPostSection.newBuilder()
                .setId(1)
                .setPos(this.section.get().getPos())
                .setContentType(PostContentType.IMAGE)
                .setContent(this.section.get().getContent())
                .build();

        BlogPostSection sectionDto2 = BlogPostSection.newBuilder()
                .setId(2)
                .setPos(section2.getPos())
                .setContentType(PostContentType.TEXT)
                .setContent(section2.getContent())
                .build();

        BlogPostSection sectionDto3 = BlogPostSection.newBuilder()
                .setId(3)
                .setPos(section3.getPos())
                .setContentType(PostContentType.CODE)
                .setContent(section3.getContent())
                .build();

        this.sectionDtos.add(this.sectionDto);
        this.sectionDtos.add(sectionDto2);
        this.sectionDtos.add(sectionDto3);
    }

    @Test
    public void testFindOneSuccess() throws Exception{
        Mockito.doReturn(this.section).when(this.repository).findById(1l);

        FindOnePostSectionRequest req = FindOnePostSectionRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostSectionResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostSectionResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostSectionResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.sectionDto, result.getData());
    }

    @Test
    public void testFindOneNotFound() throws Exception {
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1l);

        FindOnePostSectionRequest req = FindOnePostSectionRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostSectionResponse> res = StreamRecorder.create();

        service.findOne(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostSectionResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostSectionResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertEquals(BlogPostSection.newBuilder().build(), result.getData());
    }

    @Test
    public void testCreateSuccess() throws Exception{
        Mockito.doReturn(this.section.get()).when(this.repository).save(Mockito.any());

        CreatePostSectionRequest req = CreatePostSectionRequest.newBuilder()
                .setPostId(1)
                .setPos(this.sectionDto.getPos())
                .setContentType(this.sectionDto.getContentType())
                .setContent(this.sectionDto.getContent())
                .build();

        StreamRecorder<BlogPostSectionResponse> res = StreamRecorder.create();

        service.create(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostSectionResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostSectionResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.CREATED.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertEquals(this.sectionDto, result.getData());
    }
}
