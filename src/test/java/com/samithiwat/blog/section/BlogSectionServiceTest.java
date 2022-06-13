package com.samithiwat.blog.section;

import com.github.javafaker.Faker;
import com.samithiwat.blog.TestConfig;
import com.samithiwat.blog.common.ContentType;
import com.samithiwat.blog.grpc.section.*;
import com.samithiwat.blog.grpc.dto.BlogPostSection;
import com.samithiwat.blog.grpc.dto.PostContentType;
import com.samithiwat.blog.section.entity.BlogSection;
import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
    private BlogSectionGrpcServiceImpl service;

    private BlogPostSection sectionDto;
    private Optional<BlogSection> section;

    @BeforeEach
    void setup(){
        Faker faker = new Faker();

        this.section = Optional.of(new BlogSection(1, ContentType.IMAGE, faker.internet().image(), 1L));
        this.section.get().setId(1L);

        this.sectionDto = BlogPostSection.newBuilder()
                .setId(1)
                .setPos(this.section.get().getPos())
                .setContentType(PostContentType.IMAGE)
                .setContent(this.section.get().getContent())
                .build();
    }

    @Test
    public void testFindOneSuccess() throws Exception{
        Mockito.doReturn(this.section).when(this.repository).findById(1L);

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
        Mockito.doReturn(Optional.empty()).when(this.repository).findById(1L);

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

    @Test
    public void testUpdateSuccess() throws Exception{
        Mockito.doReturn(true).when(this.repository).update(1, this.sectionDto.getPos(), this.sectionDto.getContent());

        UpdatePostSectionRequest req = UpdatePostSectionRequest.newBuilder()
                .setId(1)
                .setPos(this.sectionDto.getPos())
                .setContent(this.sectionDto.getContent())
                .build();

        StreamRecorder<BlogPostSectionStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostSectionStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostSectionStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        Mockito.doReturn(false).when(this.repository).update(1, this.sectionDto.getPos(), this.sectionDto.getContent());


        UpdatePostSectionRequest req = UpdatePostSectionRequest.newBuilder()
                .setId(1)
                .setPos(this.sectionDto.getPos())
                .setContent(this.sectionDto.getContent())
                .build();

        StreamRecorder<BlogPostSectionStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostSectionStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostSectionStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testDeleteSuccess() throws Exception{
        Mockito.doNothing().when(this.repository).deleteById(1L);

        DeletePostSectionRequest req = DeletePostSectionRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostSectionStatusResponse> res = StreamRecorder.create();

        service.delete(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostSectionStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostSectionStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        Mockito.doThrow(new EmptyResultDataAccessException("Not found section", 1)).when(this.repository).deleteById(1L);


        DeletePostSectionRequest req = DeletePostSectionRequest.newBuilder()
                .setId(1)
                .build();

        StreamRecorder<BlogPostSectionStatusResponse> res = StreamRecorder.create();

        service.delete(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostSectionStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostSectionStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

}
