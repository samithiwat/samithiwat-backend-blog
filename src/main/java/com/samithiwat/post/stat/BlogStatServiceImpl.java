package com.samithiwat.post.stat;

import com.samithiwat.post.grpc.blogstat.*;
import com.samithiwat.post.stat.entity.BlogStat;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

public class BlogStatServiceImpl extends BlogPostStatServiceGrpc.BlogPostStatServiceImplBase implements BlogStatService {
    @Autowired
    BlogStatRepository repository;

    public BlogStatServiceImpl() {}

    public BlogStatServiceImpl(BlogStatRepository repository) {
        this.repository = repository;
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
    public BlogStat create(Long postId) {
        BlogStat stat = new BlogStat(postId);
        return this.repository.save(stat);
    }

    @Override
    public boolean delete(Long id) {

        try{
            this.repository.deleteById(id);
            return true;
        }catch(EmptyResultDataAccessException err){
            return false;
        }
    }
}
