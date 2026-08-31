package com.sparta.moa.post.repository;

import com.sparta.moa.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository {
}
