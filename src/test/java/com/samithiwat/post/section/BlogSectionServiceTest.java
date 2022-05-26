package com.samithiwat.post.section;

import com.github.javafaker.Faker;
import com.samithiwat.post.TestConfig;
import com.samithiwat.post.bloguser.BlogUserServiceImpl;
import com.samithiwat.post.common.ContentType;
import com.samithiwat.post.grpc.dto.BlogPostSection;
import com.samithiwat.post.grpc.dto.BlogUser;
import com.samithiwat.post.grpc.dto.PostContentType;
import com.samithiwat.post.section.entity.BlogSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test-user", // Enable inProcess server
        "grpc.server.port=-1", // Disable external server
        "grpc.client.blogSectionService.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
@DirtiesContext
@ExtendWith(SpringExtension.class)
public class BlogSectionServiceTest {
    @Spy
    private BlogUserServiceImpl blogUserService;

    @Spy
    private BlogSectionRepository repository;

    private List<BlogPostSection> sectionDtos;
    private BlogPostSection sectionDto;
    private List<BlogSection> sections;
    private Optional<BlogSection> section;
    private Faker faker;

    @BeforeEach
    void setup(){
        this.faker = new Faker();

        this.sections = new ArrayList<>();
        this.section = Optional.of(new BlogSection(1, ContentType.IMAGE, faker.internet().image()));
        this.section.get().setId(1l);

        BlogSection section2 = new BlogSection(2, ContentType.TEXT, faker.lorem().paragraph());
        section2.setId(2l);

        BlogSection section3 = new BlogSection(3, ContentType.CODE, faker.lorem().paragraph());
        section2.setId(3l);

        this.sections.add(this.section.get());
        this.sections.add(section2);
        this.sections.add(section3);

        this.sectionDtos = new ArrayList<BlogPostSection>();
        this.sectionDto = BlogPostSection.newBuilder()
                .setId(1)
                .setOrder(this.section.get().getOrder())
                .setContentType(PostContentType.IMAGE)
                .setContent(this.section.get().getContent())
                .build();

        BlogPostSection sectionDto2 = BlogPostSection.newBuilder()
                .setId(2)
                .setOrder(section2.getOrder())
                .setContentType(PostContentType.TEXT)
                .setContent(section2.getContent())
                .build();

        BlogPostSection sectionDto3 = BlogPostSection.newBuilder()
                .setId(3)
                .setOrder(section3.getOrder())
                .setContentType(PostContentType.CODE)
                .setContent(section3.getContent())
                .build();

        this.sectionDtos.add(this.sectionDto);
        this.sectionDtos.add(sectionDto2);
        this.sectionDtos.add(sectionDto3);
    }
}
