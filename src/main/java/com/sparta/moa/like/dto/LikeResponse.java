package com.sparta.moa.like.dto;

public record LikeResponse(
        Long postId,
        Long likeCount,
        boolean liked
) {}