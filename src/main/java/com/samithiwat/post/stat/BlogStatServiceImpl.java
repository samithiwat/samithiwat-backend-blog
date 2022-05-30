package com.samithiwat.post.stat;

import com.samithiwat.post.grpc.blogsection.BlogPostSectionStatusResponse;
import com.samithiwat.post.grpc.blogstat.*;
import com.samithiwat.post.grpc.dto.BlogPostStat;
import com.samithiwat.post.stat.entity.BlogStat;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

public class BlogStatServiceImpl extends BlogPostStatServiceGrpc.BlogPostStatServiceImplBase {
    @Autowired
    BlogStatRepository repository;

    public BlogStatServiceImpl() {}

    public BlogStatServiceImpl(BlogStatRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(CreateBlogPostStatRequest request, StreamObserver<BlogPostStatResponse> responseObserver) {
        BlogPostStatResponse.Builder res = BlogPostStatResponse.newBuilder();

        BlogStat stat = new BlogStat(
                (long) request.getPostId()
        );

        stat = this.repository.save(stat);

        com.samithiwat.post.grpc.dto.BlogPostStat result = BlogPostStat.newBuilder()
                .setId(Math.toIntExact(stat.getId()))
                .setLikes(Math.toIntExact(stat.getLikes()))
                .setViews(Math.toIntExact(stat.getViews()))
                .setShares(Math.toIntExact(stat.getShares()))
                .build();

        res.setStatusCode(HttpStatus.CREATED.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateBlogPostStatRequest request, StreamObserver<BlogPostStatStatusResponse> responseObserver) {
        BlogPostStatStatusResponse.Builder res = BlogPostStatStatusResponse.newBuilder();

        boolean isUpdated = switch(request.getCountType()){
            case VIEW_INCREASE -> this.repository.increaseView((long) request.getId());
            case LIKE_INCREASE -> this.repository.increaseLike((long) request.getId());
            case LIKE_DECREASE -> this.repository.decreaseLike((long) request.getId());
            case SHARE_INCREASE -> this.repository.increaseShare((long) request.getId());
            default -> false;
        };

        if(!isUpdated){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found stat")
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
    public void delete(DeleteBlogPostStatRequest request, StreamObserver<BlogPostStatStatusResponse> responseObserver) {
        BlogPostStatStatusResponse.Builder res = BlogPostStatStatusResponse.newBuilder();

        try{
            this.repository.deleteById((long) request.getId());
            res.setStatusCode(HttpStatus.OK.value())
                    .setData(true);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(EmptyResultDataAccessException err){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found section")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }
}
