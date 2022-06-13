package com.samithiwat.blog.bloguser;

import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.grpc.bloguser.*;
import com.samithiwat.blog.grpc.dto.BlogUser;
import com.samithiwat.blog.grpc.dto.User;
import com.samithiwat.blog.post.BlogPostServiceImpl;
import com.samithiwat.blog.post.entity.Post;
import com.samithiwat.blog.user.UserService;
import com.samithiwat.blog.user.UserServiceImpl;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

import java.util.List;

@GrpcService
public class BlogUserGrpcServiceImpl extends BlogUserServiceGrpc.BlogUserServiceImplBase{
    @Autowired
    private BlogUserRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private BlogPostServiceImpl postService;

    public BlogUserGrpcServiceImpl(BlogUserRepository repository, UserServiceImpl userService, BlogPostServiceImpl postService){
        this.repository = repository;
        this.userService = userService;
        this.postService = postService;
    }

    @Override
    public void addBookmark(AddBookmarkRequest request, StreamObserver<BookmarkResponse> responseObserver) {
        BookmarkResponse.Builder res = BookmarkResponse.newBuilder();

        BUser user = this.repository.findById((long) request.getUserId()).orElse(null);

        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        List<Post> posts = user.getBookmarks();
        Post post = this.postService.findOneEntityById((long) request.getPostId());

        if(post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        posts.add(post);

        this.repository.save(user);

        for (Post p:posts) {
            res.addData(Math.toIntExact(p.getId()));
        }

        res.setStatusCode(HttpStatus.OK.value());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findAllBookmark(FindAllBookmarkRequest request, StreamObserver<BookmarkResponse> responseObserver) {
        BookmarkResponse.Builder res = BookmarkResponse.newBuilder();

        BUser user = this.repository.findById((long) request.getUserId()).orElse(null);

        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        List<Post> posts = user.getBookmarks();

        for (Post p:posts) {
            res.addData(Math.toIntExact(p.getId()));
        }

        res.setStatusCode(HttpStatus.OK.value());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBookmark(DeleteBookmarkRequest request, StreamObserver<BookmarkResponse> responseObserver) {
        BookmarkResponse.Builder res = BookmarkResponse.newBuilder();

        BUser user = this.repository.findById((long) request.getUserId()).orElse(null);

        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        List<Post> posts = user.getBookmarks();
        for (int i = 0; i < posts.size(); i++) {
            if(posts.get(i).getId() == request.getPostId()){
                posts.remove(i);
                break;
            }
        }

        this.repository.save(user);

        for (Post p:posts) {
            res.addData(Math.toIntExact(p.getId()));
        }

        res.setStatusCode(HttpStatus.OK.value());

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        ReadResponse.Builder res = ReadResponse.newBuilder();

        BUser user = this.repository.findById((long) request.getUserId()).orElse(null);

        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user")
                    .setData(false);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        Post post = this.postService.findOneEntityById((long) request.getPostId());

        if(post == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found post");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        List<Post> posts = user.getReads();

        posts.add(post);

        this.repository.save(user);

        res.setStatusCode(HttpStatus.OK.value()).setData(true);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void findOne(FindOneUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        BUser user = this.repository.findById((long) request.getId()).orElse(null);
        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        User userDto = this.userService.findOne(user.getUserId());

        if(userDto == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Invalid user data");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BlogUser result = BlogUser.newBuilder()
                .setId(Math.toIntExact(user.getId()))
                .setFirstname(userDto.getFirstname())
                .setLastname(userDto.getLastname())
                .setDisplayName(userDto.getDisplayName())
                .setImageUrl(userDto.getImageUrl())
                .setDescription(user.getDescription())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(CreateUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        User userDto = this.userService.findOne((long) request.getUserId());

        if(userDto == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        BUser dto = new BUser();
        dto.setUserId((long) request.getUserId());
        dto.setDescription(request.getDescription());

        try{
            BUser user = this.repository.save(dto);
            BlogUser result = BlogUser.newBuilder()
                    .setId(Math.toIntExact(user.getId()))
                    .setFirstname(userDto.getFirstname())
                    .setLastname(userDto.getLastname())
                    .setDisplayName(userDto.getDisplayName())
                    .setImageUrl(userDto.getImageUrl())
                    .setDescription(user.getDescription())
                    .build();
            res.setStatusCode(HttpStatus.CREATED.value())
                    .setData(result);
        }catch(DataIntegrityViolationException err){
            res.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .addErrors("Duplicated User ID");
        }

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        BUser user = this.repository.findById(Long.valueOf(request.getId())).map(u -> {
            u.setDescription(request.getDescription());
            return this.repository.save(u);
        }).orElse(null);

        if(user == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.blog.grpc.dto.BlogUser result = com.samithiwat.blog.grpc.dto.BlogUser.newBuilder()
                .setId(Math.toIntExact(user.getId()))
                .setDescription(user.getDescription())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteUserRequest request, StreamObserver<BlogUserResponse> responseObserver) {
        BlogUserResponse.Builder res = BlogUserResponse.newBuilder();

        try{
            this.repository.deleteById((long) request.getId());
            res.setStatusCode(HttpStatus.NO_CONTENT.value())
                    .setData(BlogUser.newBuilder().build());

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(EmptyResultDataAccessException err){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found user");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }
}
