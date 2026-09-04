package com.sparta.moa.like.repository;

import com.sparta.moa.like.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);

    long countByPostId(Long postId);

    @Query("select pl.post.id from PostLike pl " +
            "where pl.member.id = :memberId and pl.post.id in :postIds")
    Set<Long> findLikedPostIds(@Param("memberId") Long memberId,
                               @Param("postIds") List<Long> postIds);
}