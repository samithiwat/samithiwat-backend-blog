package com.samithiwat.post.post;

import com.samithiwat.post.post.entity.BlogPost;

public interface BlogPostService {
    BlogPost findOneEntityById(Long Id);
    BlogPost findOneEntityBySlug(String slug);
}
