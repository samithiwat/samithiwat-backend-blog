package com.samithiwat.post.section;

import com.samithiwat.post.common.ContentType;
import com.samithiwat.post.grpc.blogsection.*;
import com.samithiwat.post.grpc.dto.BlogPostSection;
import com.samithiwat.post.grpc.dto.PostContentType;
import com.samithiwat.post.section.entity.BlogSection;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

public class BlogSectionServiceImpl extends BlogPostSectionServiceGrpc.BlogPostSectionServiceImplBase {
    @Autowired
    BlogSectionRepository repository;

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
                .setContentTypeValue(RawToDtoContentType(section.getContentType()).getNumber())
                .setContent(section.getContent())
                .build();

        res.setStatusCode(HttpStatus.OK.value())
                .setData(result);

        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(CreatePostSectionRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        BlogPostSectionResponse.Builder res = BlogPostSectionResponse.newBuilder();

        // TODO: Implement relationship with post

        BlogSection sectionDto = new BlogSection(
                request.getOrder(),
                DtoToRawContentType(request.getContentType()),
                request.getContent()
        );

        try{
            BlogSection section = this.repository.save(sectionDto);
            com.samithiwat.post.grpc.dto.BlogPostSection result = BlogPostSection.newBuilder()
                    .setId(Math.toIntExact(section.getId()))
                    .setOrder(section.getOrder())
                    .setContentTypeValue(RawToDtoContentType(section.getContentType()).getNumber())
                    .setContent(section.getContent())
                    .build();

            res.setStatusCode(HttpStatus.CREATED.value())
                    .setData(result);

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }catch(DataIntegrityViolationException err){
            res.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .addErrors("Duplicated slug");

            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void update(UpdatePostSectionRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.update(request, responseObserver);
    }

    @Override
    public void delete(DeletePostSectionRequest request, StreamObserver<BlogPostSectionResponse> responseObserver) {
        super.delete(request, responseObserver);
    }

    private ContentType DtoToRawContentType(PostContentType contentType){
        switch(contentType){
            case TEXT:
                return ContentType.TEXT;
            case IMAGE:
                return ContentType.IMAGE;
            case CODE:
                return ContentType.CODE;
            default:
                return null;
        }
    }

    private PostContentType RawToDtoContentType(ContentType contentType){
       return switch (contentType){
           case TEXT -> PostContentType.TEXT;
           case IMAGE -> PostContentType.IMAGE;
           case CODE -> PostContentType.CODE;
           default -> null;
        };
    }
}
