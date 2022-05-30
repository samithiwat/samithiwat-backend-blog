package com.samithiwat.post.stat;

import com.samithiwat.post.stat.entity.BlogStat;

public interface BlogStatService {
    BlogStat create(Long postId);
    boolean delete(Long id);
}
