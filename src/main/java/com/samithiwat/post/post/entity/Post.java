package com.samithiwat.post.post.entity;

import com.samithiwat.post.bloguser.entity.BlogUser;
import com.samithiwat.post.comment.entity.Comment;
import com.samithiwat.post.section.entity.BlogSection;
import com.samithiwat.post.stat.entity.BlogStat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "post", indexes = @Index(columnList = "slug"))
@SQLDelete(sql = "UPDATE user SET deletedDate = CURRENT_DATE WHERE id = ?")
@Where(clause = "deletedDate IS NULL")
public class Post {
    public Post() {}

    public Post(BlogUser author, String slug, String summary, Boolean isPublished, Instant publishDate) {
        this.setAuthor(author);
        this.setSlug(slug);
        this.setSummary(summary);
        this.setPublished(isPublished);
        this.setPublishDate(publishDate);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER)
    private List<Comment> comments;

    @OneToOne(mappedBy = "post", cascade = CascadeType.REMOVE)
    private BlogStat stat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private BlogUser author;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER)
    private List<BlogSection> sections;

    @Column(unique = true)
    private String slug;

    @Column
    private String summary;

    @Column
    private Boolean isPublished;

    @Column
    private Instant publishDate;

    @CreationTimestamp
    private Instant createdDate;

    @UpdateTimestamp
    private Instant updatedDate;

    @Column
    private Instant deletedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BlogStat getStat() {
        return stat;
    }

    public void setStat(BlogStat stat) {
        this.stat = stat;
    }

    public BlogUser getAuthor() {
        return author;
    }

    public void setAuthor(BlogUser author) {
        this.author = author;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<BlogSection> getSections() {
        return sections;
    }

    public void setSections(List<BlogSection> sections) {
        this.sections = sections;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Boolean getPublished() {
        return isPublished;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public Instant getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Instant publishDate) {
        this.publishDate = publishDate;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Instant deletedDate) {
        this.deletedDate = deletedDate;
    }
}
