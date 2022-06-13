package com.samithiwat.blog.stat.entity;

import com.samithiwat.blog.post.entity.Post;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stat")
@SQLDelete(sql = "UPDATE user SET deletedDate = CURRENT_DATE WHERE id = ?")
@Where(clause = "deletedDate IS NULL")
public class BlogStat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name="post_id")
    private Post post;

    @Column
    private Long likes;

    @Column
    private Long views;

    @Column
    private Long shares;

    @CreationTimestamp
    private Instant createdDate;

    @UpdateTimestamp
    private Instant updatedDate;

    @Column
    private Instant deletedDate;

    public BlogStat(){}

    public BlogStat(Long postId){
        Post post = new Post();
        post.setId(postId);
        setPost(post);
    }

    public BlogStat(Long likes, Long views, Long shares, Long postId){
        Post post = new Post();
        post.setId(postId);
        setPost(post);
        setLikes(likes);
        setShares(shares);
        setViews(views);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getShares() {
        return shares;
    }

    public void setShares(Long shares) {
        this.shares = shares;
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
