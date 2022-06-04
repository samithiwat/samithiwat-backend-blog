package com.samithiwat.blog.post;

import com.samithiwat.blog.bloguser.BlogUserServiceImpl;
import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.grpc.post.*;
import com.samithiwat.blog.grpc.common.PaginationMetadata;
import com.samithiwat.blog.grpc.dto.BlogUser;
import com.samithiwat.blog.post.entity.Post;
import com.samithiwat.blog.stat.BlogStatServiceImpl;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class BlogPostServiceGrpcImpl extends BlogPostServiceGrpc.BlogPostServiceImplBase {
    @Autowired
    private BlogPostRepository repository;

    @Autowired
    private BlogUserServiceImpl userService;

    @Autowired
    private BlogStatServiceImpl statService;

    public BlogPostServiceGrpcImpl(BlogPostRepository repository, BlogUserServiceImpl userService, BlogStatServiceImpl statService) {
        this.repository = repository;
        this.userService = userService;
        this.statService = statService;
    }

    @Override
    public void findAll(FindAllPostRequest request, StreamObserver<BlogPostPaginationResponse> responseObserver) {
        int limit = Math.toIntExact(request.getLimit()), page = Math.toIntExact(request.getPage());

        if(limit < 5 ){
            limit = 5;
        }

        if(limit > 20){
            limit = 20;
        }

        if(page < 1){
            page = 1;
        }

        BlogPostPaginationResponse.Builder res = BlogPostPaginationResponse.newBuilder();

        Page<Post> blogPostPage = this.repository.findAll(PageRequest.of(page - 1, limit));

        PaginationMetadata metadata = PaginationMetadata.newBuilder()
                .setTotalItem(blogPostPage.getTotalElements())
                .setItemCount(blogPostPage.getNumberOfElements())
                .setItemsPerPage(blogPostPage.getSize())
                .setTotalPage(blogPostPage.getTotalPages())
                .setCurrentPage(blogPostPage.getNumber() + 1)
                .build();

        BlogPostPagination.Builder result = BlogPostPagination.newBuilder()
                .setMeta(metadata);

        for(Post post: blogPostPage.getContent()) {
            BlogUser userDto = this.userService.findOne(post.getAuthor().getUserId());

            com.samithiwat.blog.grpc.dto.BlogPost dto = com.samithiwat.blog.grpc.dto.BlogPost.newBuilder()
                    .setId(Math.toIntExact(post.getId()))
                    .setAuthor(userDto)
                    .setSlug(post.getSlug())
                    .setSummary(post.getSummary())
                    .setIsPublish(post.getPublished())
                    .setPublishDate(post.getPublishDate().toString())
                    .build();

            result.addItems(dto);
        }

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result.build());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findOne(FindOnePostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        BlogPostResponse.Builder res = BlogPostResponse.newBuilder();

        Post post = this.repository.findById((long) request.getId()).orElse(null);

        if (post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogUser user = this.userService.findOne(post.getAuthor().getUserId());

        if (user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.blog.grpc.dto.BlogPost result = com.samithiwat.blog.grpc.dto.BlogPost.newBuilder()
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

        Post post = this.repository.findBySlug(request.getSlug()).orElse(null);

        if (post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogUser user = this.userService.findOne(post.getAuthor().getUserId());

        if (user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.blog.grpc.dto.BlogPost result = com.samithiwat.blog.grpc.dto.BlogPost.newBuilder()
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

        BUser user = this.userService.findOneOrCreate((long) userDto.getId());
        Post post = new Post(user, request.getSlug(), request.getSummary(), request.getIsPublish(), Instant.parse(request.getPublishDate()));

        try{
            post = this.repository.save(post);
            this.statService.create(post.getId());

            com.samithiwat.blog.grpc.dto.BlogPost result = com.samithiwat.blog.grpc.dto.BlogPost.newBuilder()
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
