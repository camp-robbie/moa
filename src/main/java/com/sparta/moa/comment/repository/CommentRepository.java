package com.sparta.moa.comment.repository;

import com.sparta.moa.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryRepository {
//    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
