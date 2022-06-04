package com.samithiwat.post.tag;

import com.samithiwat.post.bloguser.BlogUserServiceImpl;
import com.samithiwat.post.grpc.dto.BlogPost;
import com.samithiwat.post.grpc.dto.BlogPostStat;
import com.samithiwat.post.grpc.dto.BlogTag;
import com.samithiwat.post.grpc.dto.BlogUser;
import com.samithiwat.post.grpc.tag.*;
import com.samithiwat.post.post.entity.Post;
import com.samithiwat.post.tag.entity.Tag;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

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
    public void update(UpdateTagRequest request, StreamObserver<BlogTagResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void delete(DeleteTagRequest request, StreamObserver<BlogTagStatusResponse> responseObserver) {
        super.delete(request, responseObserver);
    }
}
