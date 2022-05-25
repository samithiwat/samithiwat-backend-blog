package com.samithiwat.post.post;

import com.samithiwat.post.grpc.blogpost.*;
import io.grpc.stub.StreamObserver;

public class PostServiceImpl extends BlogPostServiceGrpc.BlogPostServiceImplBase {
    @Override
    public void findAll(FindAllPostRequest request, StreamObserver<BlogPostPaginationResponse> responseObserver) {
        super.findAll(request, responseObserver);
    }

    @Override
    public void findOne(FindOnePostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        super.findOne(request, responseObserver);
    }

    @Override
    public void create(CreatePostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        super.create(request, responseObserver);
    }

    @Override
    public void update(UpdatePostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void delete(DeletePostRequest request, StreamObserver<BlogPostResponse> responseObserver) {
        super.delete(request, responseObserver);
    }
}
