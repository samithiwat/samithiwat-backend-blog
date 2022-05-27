package com.samithiwat.post.post;

import com.samithiwat.post.bloguser.BlogUserServiceImpl;
import com.samithiwat.post.grpc.blogpost.*;
import com.samithiwat.post.grpc.dto.BlogUser;
import com.samithiwat.post.post.entity.BlogPost;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class BlogPostServiceImpl extends BlogPostServiceGrpc.BlogPostServiceImplBase {
    @Autowired
    private BlogPostRepository repository;

    @Autowired
    private BlogUserServiceImpl userService;

    public BlogPostServiceImpl(){}

    public BlogPostServiceImpl(BlogPostRepository repository, BlogUserServiceImpl userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Override
    public void findAll(FindAllPostRequest request, StreamObserver<BlogPostPaginationResponse> responseObserver) {
        super.findAll(request, responseObserver);
    }

    @Override
    public void findOne(FindOnePostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        BlogPostResponse.Builder res = BlogPostResponse.newBuilder();

        BlogPost post = this.repository.findById((long) request.getId()).orElse(null);

        if (post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogUser user = this.userService.findOne(post.getAuthor().getId());

        if (user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.post.grpc.dto.BlogPost result = com.samithiwat.post.grpc.dto.BlogPost.newBuilder()
                .setId(Math.toIntExact(post.getId()))
                .setAuthor(user)
                .setSlug(post.getSlug())
                .setSummary(post.getSummary())
                .setIsPublish(post.getPublished())
                .setPublishDate(post.getPublishDate().toString())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findBySlug(FindBySlugPostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        BlogPostResponse.Builder res = BlogPostResponse.newBuilder();

        BlogPost post = this.repository.findBySlug(request.getSlug()).orElse(null);

        if (post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogUser user = this.userService.findOne(post.getAuthor().getId());

        if (user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.post.grpc.dto.BlogPost result = com.samithiwat.post.grpc.dto.BlogPost.newBuilder()
                .setId(Math.toIntExact(post.getId()))
                .setAuthor(user)
                .setSlug(post.getSlug())
                .setSummary(post.getSummary())
                .setIsPublish(post.getPublished())
                .setPublishDate(post.getPublishDate().toString())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(CreatePostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        BlogPostResponse.Builder res = BlogPostResponse.newBuilder();

        BlogUser userDto = this.userService.findOne((long) request.getAuthorId());

        if (userDto == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.post.bloguser.entity.BlogUser user = this.userService.findOneEntityByUserId((long) userDto.getId());
        BlogPost post = new BlogPost(user, request.getSlug(), request.getSummary(), request.getIsPublish(), Instant.parse(request.getPublishDate()));

        try{
            post = this.repository.save(post);
            com.samithiwat.post.grpc.dto.BlogPost result = com.samithiwat.post.grpc.dto.BlogPost.newBuilder()
                    .setId(Math.toIntExact(post.getId()))
                    .setAuthor(userDto)
                    .setSlug(post.getSlug())
                    .setSummary(post.getSummary())
                    .setIsPublish(post.getPublished())
                    .setPublishDate(post.getPublishDate().toString())
                    .build();

            res.setStatusCode(HttpStatus.CREATED.value())
                    .setData(result);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch (DataIntegrityViolationException err){
            res.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .addErrors("Duplicated slug");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void update(UpdatePostRequest request, StreamObserver<BlogPostStatusResponse> responseObserver) {
        BlogPostStatusResponse.Builder res = BlogPostStatusResponse.newBuilder();

        try{
            boolean isUpdate = this.repository.update(request.getId(), request.getSlug(), request.getSummary(), request.getIsPublish(), Instant.parse(request.getPublishDate()));
            if(!isUpdate){
                res.setStatusCode(HttpStatus.NOT_FOUND.value())
                        .addErrors("Not found post")
                        .setData(false);

                responseObserver.onNext(res.build());
                responseObserver.onCompleted();
                return;
            }

            res.setStatusCode(HttpStatus.OK.value())
                    .setData(true);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(DataIntegrityViolationException err){
            res.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .addErrors("Duplicated slug")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }


    }

    @Override
    public void delete(DeletePostRequest request, StreamObserver<BlogPostStatusResponse> responseObserver) {
        BlogPostStatusResponse.Builder res = BlogPostStatusResponse.newBuilder();

        try{
            this.repository.deleteById((long) request.getId());
            res.setStatusCode(HttpStatus.OK.value())
                    .setData(true);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(EmptyResultDataAccessException err){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }
}
