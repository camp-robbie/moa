package com.sparta.moa.like;

import com.sparta.moa.like.repository.PostLikeRepository;
import com.sparta.moa.like.service.PostLikeFacade;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.member.repository.MemberRepository;
import com.sparta.moa.post.entity.Post;
import com.sparta.moa.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PostLikeConcurrencyTest {

    @Autowired
    PostLikeFacade postLikeFacade;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostLikeRepository postLikeRepository;
    @Autowired
    MemberRepository memberRepository;

    Long postId;
    List<Long> memberIds;

    @BeforeEach
    void setUp() {
        postLikeRepository.deleteAllInBatch();

        Post post = postRepository.findById(1L).orElseThrow();
        postRepository.resetLikeCount(post.getId());
        postId = post.getId();

        memberIds = memberRepository.findTop100ByOrderByIdAsc()
                .stream().map(Member::getId).toList();
    }

    @Test
    void 서로_다른_100명이_동시에_누르면_100이다() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(memberIds.size());

        for (Long memberId : memberIds) {
            executor.submit(() -> {
                try {
                    postLikeFacade.like(postId, memberId);
                } catch (Exception ignored) {
                    // 실패는 일단 무시하고 숫자만 본다
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        Post post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getLikeCount()).isEqualTo(100L);
    }

    @Test
    void 같은_사람이_동시에_10번_누르면_1개만_저장된다() throws Exception {

        Long memberId = memberIds.get(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    postLikeFacade.like(postId, memberId);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(1L);
    }
}
