package com.samithiwat.post.section;

import com.samithiwat.post.grpc.blogsection.*;
import io.grpc.stub.StreamObserver;

public class BlogSectionServiceImpl extends BlogPostSectionServiceGrpc.BlogPostSectionServiceImplBase {
    @Override
    public void findOne(FindOnePostRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.findOne(request, responseObserver);
    }

    @Override
    public void create(CreatePostRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.create(request, responseObserver);
    }

    @Override
    public void update(UpdatePostRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void delete(DeletePostRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.delete(request, responseObserver);
    }
}
