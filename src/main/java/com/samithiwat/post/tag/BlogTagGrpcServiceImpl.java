package com.samithiwat.post.tag;

import com.samithiwat.post.comment.entity.Comment;
import com.samithiwat.post.grpc.blogcomment.BlogCommentListResponse;
import com.samithiwat.post.grpc.dto.BlogComment;
import com.samithiwat.post.grpc.dto.BlogTag;
import com.samithiwat.post.grpc.tag.*;
import com.samithiwat.post.post.entity.Post;
import com.samithiwat.post.tag.entity.Tag;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;

public class BlogTagGrpcServiceImpl extends BlogTagServiceGrpc.BlogTagServiceImplBase {
    @Autowired
    BlogTagRepository repository;

    public BlogTagGrpcServiceImpl(BlogTagRepository repository) {
        this.repository = repository;
    }

    @Override
    public void findAll(FindAllTagRequest request, StreamObserver<BlogTagListResponse> responseObserver) {
        BlogTagListResponse.Builder res = BlogTagListResponse.newBuilder();

        Sort sort = switch (request.getSortType()){
            case ALPHABET_ASC -> Sort.by(Sort.Direction.ASC, "name");
            case ALPHABET_DESC -> Sort.by(Sort.Direction.DESC, "name");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };

        Iterable<Tag> tags = this.repository.findAll(sort);

        for (Tag tag: tags) {
            BlogTag result = BlogTag.newBuilder()
                    .setId(Math.toIntExact(tag.getId()))
                    .setName(tag.getName())
                    .build();

            res.addData(result);
        }

        res.setStatusCode(HttpStatus.OK.value());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findOne(FindOneTagRequest request, StreamObserver<BlogTagResponse> responseObserver) {
        super.findOne(request, responseObserver);
    }

    @Override
    public void create(CreateTagRequest request, StreamObserver<BlogTagResponse> responseObserver) {
        super.create(request, responseObserver);
    }

    @Override
    public void update(UpdateTagRequest request, StreamObserver<BlogTagResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void delete(DeleteTagRequest request, StreamObserver<BlogTagStatusResponse> responseObserver) {
        super.delete(request, responseObserver);
    }
}
