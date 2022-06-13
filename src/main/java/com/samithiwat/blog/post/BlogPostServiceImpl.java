package com.samithiwat.blog.post;

import com.samithiwat.blog.post.entity.Post;
import org.springframework.stereotype.Service;

// TODO: Handle draft by isPublish

@Service
public class BlogPostServiceImpl implements BlogPostService {
    private BlogPostRepository repository;

    public BlogPostServiceImpl(BlogPostRepository repository){
        this.repository = repository;
    }

    @Override
    public Post findOneEntityById(Long id) {
        return this.repository.findById(id).orElse(null);
    }

    @Override
    public Post findOneEntityBySlug(String slug) {
        return this.repository.findBySlug(slug).orElse(null);
    }
}
