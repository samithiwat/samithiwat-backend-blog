package com.samithiwat.post.tag;

import com.samithiwat.post.tag.entity.Tag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BlogTagRepository extends CrudRepository<Tag, Long>, PagingAndSortingRepository<Tag, Long> {
}
