package com.samithiwat.blog.bloguser.entity;

import com.samithiwat.blog.post.entity.Post;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "user")
@SQLDelete(sql = "UPDATE user SET deletedDate = CURRENT_DATE WHERE id = ?")
@Where(clause = "deletedDate IS NULL")
public class BUser {
    public BUser() {}

    public BUser(Long userId) {
        this.userId = userId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private Long userId;

    @OneToMany(mappedBy = "author")
    private List<Post> posts;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_post_bookmark",
            joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="post_id", referencedColumnName = "id")
    )
    private List<Post> bookmarks;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_post_read",
            joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="post_id",referencedColumnName = "id")
    )
    private List<Post> reads;

    @Column
    private String description;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Post> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Post> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public List<Post> getReads() {
        return reads;
    }

    public void setReads(List<Post> reads) {
        this.reads = reads;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
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
