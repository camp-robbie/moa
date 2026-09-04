package com.sparta.moa.like.service;

import com.sparta.moa.common.exception.ConflictException;
import com.sparta.moa.common.exception.NotFoundException;
import com.sparta.moa.like.dto.LikeResponse;
import com.sparta.moa.like.entity.PostLike;
import com.sparta.moa.like.repository.PostLikeRepository;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import com.sparta.moa.post.entity.Post;
import com.sparta.moa.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LikeResponse like(Long postId, Long memberId) {

        if (postLikeRepository.existsByPostIdAndMemberId(postId, memberId)) {
            throw new ConflictException("이미 좋아요를 누른 게시글입니다");
        }

        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + memberId));

        postLikeRepository.save(new PostLike(post, member));
        post.increaseLikeCount();

        return new LikeResponse(post.getId(), post.getLikeCount(), true);
    }

    @Transactional
    public LikeResponse unlike(Long postId, Long memberId) {

        PostLike postLike = postLikeRepository
                .findByPostIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new NotFoundException("좋아요를 누르지 않은 게시글입니다"));

        // postLike.getPost() 는 잠기지 않은 조회라 여기서 다시 잠그고 가져옵니다
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        postLikeRepository.delete(postLike);
        post.decreaseLikeCount();

        return new LikeResponse(post.getId(), post.getLikeCount(), false);
    }

    public Set<Long> findLikedPostIds(Long memberId, List<Long> postIds) {
        if (memberId == null || postIds.isEmpty()) {
            return Set.of();
        }
        return postLikeRepository.findLikedPostIds(memberId, postIds);
    }
}