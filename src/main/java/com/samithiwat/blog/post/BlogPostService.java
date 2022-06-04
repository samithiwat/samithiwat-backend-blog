package com.samithiwat.blog.post;

import com.samithiwat.blog.post.entity.Post;

public interface BlogPostService {
    Post findOneEntityById(Long Id);
    Post findOneEntityBySlug(String slug);
}
