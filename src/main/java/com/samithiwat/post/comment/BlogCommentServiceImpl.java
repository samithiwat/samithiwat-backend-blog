package com.samithiwat.post.comment;

import com.samithiwat.post.comment.entity.Comment;
import com.samithiwat.post.grpc.blogcomment.*;
import com.samithiwat.post.grpc.dto.BlogComment;
import com.samithiwat.post.post.BlogPostServiceImpl;
import com.samithiwat.post.post.entity.Post;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

public class BlogCommentServiceImpl extends BlogCommentServiceGrpc.BlogCommentServiceImplBase {
    @Autowired
    BlogCommentRepository repository;

    @Autowired
    BlogPostServiceImpl blogPostService;

    public BlogCommentServiceImpl(BlogCommentRepository repository, BlogPostServiceImpl blogPostService){
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
    public void delete(DeleteCommentRequest request, StreamObserver<BlogCommentStatusResponse> responseObserver) {
        super.delete(request, responseObserver);
    }
}
