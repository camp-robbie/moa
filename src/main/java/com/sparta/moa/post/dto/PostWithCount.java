package com.sparta.moa.post.dto;

import com.sparta.moa.post.entity.Post;

public record PostWithCount(
        Post post,
        Long commentCount
) {}
