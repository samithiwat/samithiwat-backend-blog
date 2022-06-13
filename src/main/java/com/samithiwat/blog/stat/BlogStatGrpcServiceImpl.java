package com.samithiwat.blog.stat;

import com.samithiwat.blog.grpc.stat.*;
import com.samithiwat.blog.stat.entity.BlogStat;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@GrpcService
public class BlogStatGrpcServiceImpl extends BlogPostStatServiceGrpc.BlogPostStatServiceImplBase implements BlogStatService {
    @Autowired
    BlogStatRepository repository;

    public BlogStatGrpcServiceImpl() {}

    public BlogStatGrpcServiceImpl(BlogStatRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void update(UpdateBlogPostStatRequest request, StreamObserver<BlogPostStatStatusResponse> responseObserver) {
        BlogPostStatStatusResponse.Builder res = BlogPostStatStatusResponse.newBuilder();

        boolean isUpdated = switch(request.getCountType()){
            case VIEW_INCREASE -> this.repository.increaseView((long) request.getId()) > 0;
            case LIKE_INCREASE -> this.repository.increaseLike((long) request.getId()) > 0;
            case LIKE_DECREASE -> this.repository.decreaseLike((long) request.getId()) > 0;
            case SHARE_INCREASE -> this.repository.increaseShare((long) request.getId()) > 0;
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
