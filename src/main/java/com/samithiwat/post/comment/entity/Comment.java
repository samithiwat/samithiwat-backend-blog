package com.samithiwat.post.comment.entity;

import com.samithiwat.post.post.entity.Post;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comment")
@SQLDelete(sql = "UPDATE user SET deletedDate = CURRENT_DATE WHERE id = ?")
@Where(clause = "deletedDate IS NULL")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column
    private String content;

    @Column
    private Long likes;

    @CreationTimestamp
    private Instant createdDate;

    @UpdateTimestamp
    private Instant updatedDate;

    @Column
    private Instant deletedDate;

    public Comment() {}

    public Comment(String content) {
        setContent(content);
        setLikes(0L);
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
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
}
