package com.sparta.moa.post.repository;

import com.sparta.moa.post.dto.PostSearchCondition;
import com.sparta.moa.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostQueryRepository {
    Page<Post> search(PostSearchCondition condition, Pageable pageable);
}
