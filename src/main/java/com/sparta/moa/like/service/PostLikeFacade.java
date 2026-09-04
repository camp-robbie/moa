package com.sparta.moa.like.service;

import com.sparta.moa.like.dto.LikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikeFacade {

    private final PostLikeService postLikeService;

    public LikeResponse like(Long postId, Long memberId) {
        return postLikeService.like(postId, memberId);
    }

    public LikeResponse unlike(Long postId, Long memberId) {
        return postLikeService.unlike(postId, memberId);
    }
}
