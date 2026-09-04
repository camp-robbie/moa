package com.sparta.moa.comment.repository;

import com.sparta.moa.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryRepository {
    //    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    @Modifying
    @Query("delete from Comment c where c.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
