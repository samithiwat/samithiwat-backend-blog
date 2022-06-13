package com.samithiwat.blog.tag;

import com.samithiwat.blog.bloguser.BlogUserServiceImpl;
import com.samithiwat.blog.grpc.dto.BlogPost;
import com.samithiwat.blog.grpc.dto.BlogPostStat;
import com.samithiwat.blog.grpc.dto.BlogTag;
import com.samithiwat.blog.grpc.dto.BlogUser;
import com.samithiwat.blog.grpc.tag.*;
import com.samithiwat.blog.post.entity.Post;
import com.samithiwat.blog.tag.entity.Tag;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@GrpcService
public class BlogTagGrpcServiceImpl extends BlogTagServiceGrpc.BlogTagServiceImplBase {
    @Autowired
    BlogTagRepository repository;

    @Autowired
    BlogUserServiceImpl userService;

    public BlogTagGrpcServiceImpl(BlogTagRepository repository, BlogUserServiceImpl userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Override
    public void findAll(FindAllTagRequest request, StreamObserver<BlogTagListResponse> responseObserver) {
        BlogTagListResponse.Builder res = BlogTagListResponse.newBuilder();

        Sort sort = switch (request.getSortType()){
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
        BlogTagResponse.Builder res = BlogTagResponse.newBuilder();

        Tag tag = this.repository.findById((long) request.getId()).orElse(null);

        if(tag == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found tag");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogTag.Builder result = BlogTag.newBuilder()
                .setId(Math.toIntExact(tag.getId()))
                .setName(tag.getName());

        for (Post post: tag.getPosts()) {
            BlogUser u = this.userService.findOne(post.getAuthor().getUserId());

            BlogPostStat s = BlogPostStat.newBuilder()
                    .setViews(Math.toIntExact(post.getStat().getViews()))
                    .setLikes(Math.toIntExact(post.getStat().getLikes()))
                    .setShares(Math.toIntExact(post.getStat().getShares()))
                    .build();

            BlogPost p = BlogPost.newBuilder()
                    .setId(Math.toIntExact(post.getId()))
                    .setSummary(post.getSummary())
                    .setStat(s)
                    .setAuthor(u)
                    .setPublishDate(post.getPublishDate().toString())
                    .build();

            result.addPosts(p);
        }


        res.setStatusCode(HttpStatus.OK.value())
                .setData(result.build());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(CreateTagRequest request, StreamObserver<BlogTagResponse> responseObserver) {
        BlogTagResponse.Builder res = BlogTagResponse.newBuilder();

        Tag tag = new Tag(request.getName());

        try{
            tag = this.repository.save(tag);

            BlogTag.Builder result = BlogTag.newBuilder()
                    .setId(Math.toIntExact(tag.getId()))
                    .setName(tag.getName());

            res.setStatusCode(HttpStatus.CREATED.value())
                    .setData(result.build());

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(DataIntegrityViolationException err){
            res.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .addErrors("Duplicated tag name");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    @Transactional
    public void update(UpdateTagRequest request, StreamObserver<BlogTagStatusResponse> responseObserver) {
        BlogTagStatusResponse.Builder res = BlogTagStatusResponse.newBuilder();

        boolean isUpdated = this.repository.update((long) request.getId(), request.getName()) > 0;

        if(!isUpdated){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found tag")
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
    public void delete(DeleteTagRequest request, StreamObserver<BlogTagStatusResponse> responseObserver) {
        BlogTagStatusResponse.Builder res = BlogTagStatusResponse.newBuilder();

        try{
            this.repository.deleteById((long) request.getId());
            res.setStatusCode(HttpStatus.OK.value())
                    .setData(true);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(EmptyResultDataAccessException err){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found tag")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }
}
