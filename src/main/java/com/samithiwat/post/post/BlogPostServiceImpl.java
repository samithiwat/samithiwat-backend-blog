package com.samithiwat.post.post;

import com.samithiwat.post.post.entity.Post;

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
