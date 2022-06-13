package com.samithiwat.blog.stat;

import com.samithiwat.blog.TestConfig;
import com.samithiwat.blog.grpc.stat.*;
import com.samithiwat.blog.grpc.dto.StatCountType;
import com.samithiwat.blog.stat.entity.BlogStat;
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
        "grpc.client.blogStatService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
class BlogStatGrpcServiceImplTest {
    @Spy
    private BlogStatRepository repository;

    @InjectMocks
    private BlogStatGrpcServiceImpl service;

    private Optional<BlogStat> stat;

    @BeforeEach
    void setup(){
        this.stat = Optional.of(new BlogStat(1000L, 2000L, 300L, 1L));
        this.stat.get().setId(1L);
    }

    @Test
    public void testCreateSuccess(){
        Mockito.doReturn(this.stat.get()).when(this.repository).save(Mockito.any());

        BlogStat stat = service.create(1L);

        Assertions.assertEquals(this.stat.get(), stat);
    }

    @Test
    public void testUpdateIncreaseView() throws Exception{
        Mockito.doReturn(1).when(this.repository).increaseView(1L);


        UpdateBlogPostStatRequest req = UpdateBlogPostStatRequest.newBuilder()
               .setId(1)
               .setCountType(StatCountType.VIEW_INCREASE)
               .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testUpdateIncreaseLike() throws Exception{
        Mockito.doReturn(1).when(this.repository).increaseLike(1L);

       UpdateBlogPostStatRequest req =UpdateBlogPostStatRequest.newBuilder()
               .setId(1)
               .setCountType(StatCountType.LIKE_INCREASE)
               .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testUpdateDecreaseLike() throws Exception{
        Mockito.doReturn(1).when(this.repository).decreaseLike(1L);

        UpdateBlogPostStatRequest req =UpdateBlogPostStatRequest.newBuilder()
                .setId(1)
                .setCountType(StatCountType.LIKE_DECREASE)
                .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }


    @Test
    public void testUpdateIncreaseShare() throws Exception{
        Mockito.doReturn(1).when(this.repository).increaseShare(1L);

        UpdateBlogPostStatRequest req =UpdateBlogPostStatRequest.newBuilder()
                .setId(1)
                .setCountType(StatCountType.SHARE_INCREASE)
                .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatusCode());
        Assertions.assertEquals(0, result.getErrorsCount());
        Assertions.assertTrue(result.getData());
    }

    @Test
    public void testUpdateIncreaseViewNotFound()throws Exception{
        Mockito.doReturn(0).when(this.repository).increaseView(1L);

        UpdateBlogPostStatRequest req = UpdateBlogPostStatRequest.newBuilder()
                .setId(1)
                .setCountType(StatCountType.VIEW_INCREASE)
                .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testUpdateIncreaseLikeNotFound() throws Exception{
        Mockito.doReturn(0).when(this.repository).increaseLike(1L);

        UpdateBlogPostStatRequest req = UpdateBlogPostStatRequest.newBuilder()
                .setId(1)
                .setCountType(StatCountType.LIKE_INCREASE)
                .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testUpdateDecreaseLikeNotFound() throws Exception{
        Mockito.doReturn(0).when(this.repository).decreaseLike(1L);

        UpdateBlogPostStatRequest req = UpdateBlogPostStatRequest.newBuilder()
                .setId(1)
                .setCountType(StatCountType.LIKE_DECREASE)
                .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testUpdateIncreaseShareNotFound()throws Exception{
        Mockito.doReturn(0).when(this.repository).increaseShare(1L);

        UpdateBlogPostStatRequest req = UpdateBlogPostStatRequest.newBuilder()
                .setId(1)
                .setCountType(StatCountType.SHARE_INCREASE)
                .build();

        StreamRecorder<BlogPostStatStatusResponse> res = StreamRecorder.create();

        service.update(req, res);

        if (!res.awaitCompletion(5, TimeUnit.SECONDS)){
            Assertions.fail();
        }

        List<BlogPostStatStatusResponse> results = res.getValues();

        Assertions.assertEquals(1, results.size());

        BlogPostStatStatusResponse result = results.get(0);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode());
        Assertions.assertEquals(1, result.getErrorsCount());
        Assertions.assertFalse(result.getData());
    }

    @Test
    public void testDeleteSuccess(){
        Mockito.doNothing().when(this.repository).deleteById(1L);

        boolean result = service.delete(1L);

        Assertions.assertTrue(result);
    }

    @Test
    public void testDeleteNotFound(){
        Mockito.doThrow(new EmptyResultDataAccessException("Not found stat", 1)).when(this.repository).deleteById(1L);

        boolean result = service.delete(1L);

        Assertions.assertFalse(result);
    }
}