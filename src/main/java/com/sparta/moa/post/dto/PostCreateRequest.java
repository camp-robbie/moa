package com.sparta.moa.post.dto;

public record PostCreateRequest(
        String title,
        String content
) {
}
