package com.samithiwat.blog.post.entity;

import com.samithiwat.blog.bloguser.entity.BUser;
import com.samithiwat.blog.comment.entity.Comment;
import com.samithiwat.blog.section.entity.BlogSection;
import com.samithiwat.blog.stat.entity.BlogStat;
import com.samithiwat.blog.tag.entity.Tag;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "post", indexes = @Index(columnList = "slug"))
@SQLDelete(sql = "UPDATE user SET deletedDate = CURRENT_DATE WHERE id = ?")
@Where(clause = "deleted_date IS NULL")
public class Post {
    public Post() {}

    public Post(BUser author, String slug, String summary, Boolean isPublished, Instant publishDate) {
        this.setAuthor(author);
        this.setSlug(slug);
        this.setSummary(summary);
        this.setPublished(isPublished);
        this.setPublishDate(publishDate);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "post")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Comment> comments;

    @OneToOne(mappedBy = "post", cascade = CascadeType.REMOVE)
    private BlogStat stat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private BUser author;

    @OneToMany(mappedBy = "post")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<BlogSection> sections;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "post_tag",
            joinColumns = @JoinColumn(name="post_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="tag_id",referencedColumnName = "id")
    )
    private List<Tag> tags;

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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public BUser getAuthor() {
        return author;
    }

    public void setAuthor(BUser author) {
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
