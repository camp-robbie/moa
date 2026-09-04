package com.sparta.moa.like.service;

import com.sparta.moa.common.exception.ConflictException;
import com.sparta.moa.like.dto.LikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikeFacade {

    private static final int MAX_ATTEMPTS = 3;

    private final PostLikeService postLikeService;

    public LikeResponse like(Long postId, Long memberId) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return postLikeService.like(postId, memberId);
            } catch (OptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ConflictException("요청이 몰리고 있습니다. 잠시 후 다시 시도해 주세요");
                }
            }
        }
        throw new IllegalStateException("도달할 수 없는 코드");
    }

    public LikeResponse unlike(Long postId, Long memberId) {
        return postLikeService.unlike(postId, memberId);
    }
}