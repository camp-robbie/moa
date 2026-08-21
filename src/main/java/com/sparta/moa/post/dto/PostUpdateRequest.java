package com.sparta.moa.post.dto;

public record PostUpdateRequest(
        String title,
        String content
) {
}
