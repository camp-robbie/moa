package com.sparta.moa.post.dto;

import com.sparta.moa.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String nickname,
        Long commentCount,
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
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
