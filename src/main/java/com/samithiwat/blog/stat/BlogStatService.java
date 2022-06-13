package com.samithiwat.blog.stat;

import com.samithiwat.blog.stat.entity.BlogStat;

public interface BlogStatService {
    BlogStat create(Long postId);
    boolean delete(Long id);
}
