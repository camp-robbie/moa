package com.sparta.moa.post.dto;

import com.sparta.moa.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String nickname,
        Long commentCount,
        Long likeCount,
        boolean liked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return from(post, null);
    }

    public static PostResponse from(Post post, Long commentCount) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getNickname(),
                commentCount,
                post.getLikeCount(),
                false,                      // liked 는 나중에 채웁니다
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public PostResponse withLiked(boolean liked) {
        return new PostResponse(id, title, content, nickname,
                commentCount, likeCount, liked, createdAt, updatedAt);
    }
}