package com.sparta.moa.post.repository;

import com.sparta.moa.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.likeCount = 0 where p.id = :id")
    void resetLikeCount(@Param("id") Long id);
}
