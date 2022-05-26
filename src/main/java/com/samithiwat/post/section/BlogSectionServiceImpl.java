package com.samithiwat.post.section;

import com.samithiwat.post.common.ContentType;
import com.samithiwat.post.grpc.blogsection.*;
import com.samithiwat.post.grpc.dto.BlogPostSection;
import com.samithiwat.post.grpc.dto.PostContentType;
import com.samithiwat.post.section.entity.BlogSection;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class BlogSectionServiceImpl extends BlogPostSectionServiceGrpc.BlogPostSectionServiceImplBase {
    @Autowired
    BlogSectionRepository repository;

    public BlogSectionServiceImpl(){}

    public BlogSectionServiceImpl(BlogSectionRepository repository){
        this.repository = repository;
    }

    @Override
    public void findOne(FindOnePostSectionRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        BlogPostSectionResponse.Builder res = BlogPostSectionResponse.newBuilder();

        BlogSection section = this.repository.findById(Long.valueOf(request.getId())).orElse(null);
        if(section == null){
            res.setStatusCode(HttpStatus.NOT_FOUND.value())
                    .addErrors("Not found section");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
            return;
        }

        com.samithiwat.post.grpc.dto.BlogPostSection result = BlogPostSection.newBuilder()
                .setId(Math.toIntExact(section.getId()))
                .setOrder(section.getOrder())
                .setContentTypeValue(RawToDtoContentType(section.getContentType()))
                .setContent(section.getContent())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(CreatePostSectionRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.create(request, responseObserver);
    }

    @Override
    public void update(UpdatePostSectionRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void delete(DeletePostSectionRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.delete(request, responseObserver);
    }

    private String DtoToRawContentType(int contentType){
        switch(contentType){
            case PostContentType.TEXT_VALUE:
                return ContentType.TEXT.toString();
            case PostContentType.IMAGE_VALUE:
                return ContentType.IMAGE.toString();
            case PostContentType.CODE_VALUE:
                return ContentType.CODE.toString();
            default:
                return null;
        }
    }

    private int RawToDtoContentType(ContentType contentType){
       return switch (contentType){
           case TEXT -> PostContentType.TEXT_VALUE;
           case IMAGE -> PostContentType.IMAGE_VALUE;
           case CODE -> PostContentType.CODE_VALUE;
           default -> -1;
        };
    }
}
