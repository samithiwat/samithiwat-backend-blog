package com.samithiwat.post.post;

import com.samithiwat.post.post.entity.Post;

public interface BlogPostService {
    Post findOneEntityById(Long Id);
    Post findOneEntityBySlug(String slug);
}
