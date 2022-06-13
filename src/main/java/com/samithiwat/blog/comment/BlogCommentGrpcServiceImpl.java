package com.samithiwat.blog.comment;

import com.samithiwat.blog.comment.entity.Comment;
import com.samithiwat.blog.comment.exception.InvalidUpdateLikeTypeException;
import com.samithiwat.blog.grpc.comment.*;
import com.samithiwat.blog.grpc.dto.BlogComment;
import com.samithiwat.blog.post.BlogPostServiceImpl;
import com.samithiwat.blog.post.entity.Post;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

import java.util.List;

@GrpcService
public class BlogCommentGrpcServiceImpl extends BlogCommentServiceGrpc.BlogCommentServiceImplBase {
    @Autowired
    BlogCommentRepository repository;

    @Autowired
    BlogPostServiceImpl blogPostService;

    public BlogCommentGrpcServiceImpl(BlogCommentRepository repository, BlogPostServiceImpl blogPostService){
        this.repository = repository;
        this.blogPostService = blogPostService;
    }

    @Override
    public void findAllCommentFromPost(FindAllCommentByPostRequest request, StreamObserver<BlogCommentListResponse> responseObserver) {
        BlogCommentListResponse.Builder res = BlogCommentListResponse.newBuilder();

        Post post = this.blogPostService.findOneEntityBySlug(request.getSlug());

        if (post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        List<Comment> comments = post.getComments();

        for (Comment comment: comments) {
            BlogComment.Builder result = BlogComment.newBuilder()
                    .setId(Math.toIntExact(comment.getId()))
                    .setContent(comment.getContent())
                    .setCreatedDate(comment.getCreatedDate().toString())
                    .setUpdatedDate(comment.getUpdatedDate().toString())
                    .setLikes(Math.toIntExact(comment.getLikes()));

            res.addData(result);
        }

        res.setStatusCode(HttpStatus.OK.value());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(CreateCommentRequest request, StreamObserver<BlogCommentResponse> responseObserver) {
        BlogCommentResponse.Builder res = BlogCommentResponse.newBuilder();

        Post post = this.blogPostService.findOneEntityBySlug(request.getSlug());

        if(post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        Comment comment = new Comment(request.getContent());
        comment = this.repository.save(comment);

        BlogComment commentDto = BlogComment.newBuilder()
                .setId(Math.toIntExact(comment.getId()))
                .setContent(comment.getContent())
                .setLikes(Math.toIntExact(comment.getLikes()))
                .setUpdatedDate(comment.getUpdatedDate().toString())
                .setCreatedDate(comment.getCreatedDate().toString())
                .build();

        res.setStatusCode(HttpStatus.CREATED.value())
                .setData(commentDto);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateCommentRequest request, StreamObserver<BlogCommentResponse> responseObserver) {
        BlogCommentResponse.Builder res = BlogCommentResponse.newBuilder();

        Comment comment = this.repository.findById((long) request.getId()).orElse(null);

        if(comment == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found comment");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        comment.setContent(request.getContent());
        comment = this.repository.save(comment);

        BlogComment commentDto = BlogComment.newBuilder()
                .setId(Math.toIntExact(comment.getId()))
                .setContent(comment.getContent())
                .setLikes(Math.toIntExact(comment.getLikes()))
                .setUpdatedDate(comment.getUpdatedDate().toString())
                .setCreatedDate(comment.getCreatedDate().toString())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(commentDto);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateLikes(UpdateCommentLikeRequest request, StreamObserver<BlogCommentStatusResponse> responseObserver) {
        BlogCommentStatusResponse.Builder res = BlogCommentStatusResponse.newBuilder();

        boolean isUpdated;

        try{
            isUpdated = switch (request.getType()){
                case LIKE_INCREASE -> this.repository.increaseLike((long) request.getId()) > 0;
                case LIKE_DECREASE -> this.repository.decreaseLike((long) request.getId()) > 0;
                default -> throw new InvalidUpdateLikeTypeException();
            };
        }catch (InvalidUpdateLikeTypeException e) {
            res.setStatusCode(HttpStatus.BAD_REQUEST.value())
                    .addErrors("Invalid updated type")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        if(!isUpdated){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found comment")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        res.setStatusCode(HttpStatus.OK.value())
                .setData(true);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteCommentRequest request, StreamObserver<BlogCommentStatusResponse> responseObserver) {
        BlogCommentStatusResponse.Builder res = BlogCommentStatusResponse.newBuilder();

        try{
            this.repository.deleteById((long) request.getId());
            res.setStatusCode(HttpStatus.OK.value())
                    .setData(true);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(EmptyResultDataAccessException err){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found comment")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }
}
