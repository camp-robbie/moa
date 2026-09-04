package com.sparta.moa.post.controller;

import com.sparta.moa.common.dto.ApiResponse;
import com.sparta.moa.common.dto.PageResponse;
import com.sparta.moa.common.security.MemberDetails;
import com.sparta.moa.like.service.PostLikeService;
import com.sparta.moa.post.dto.PostCreateRequest;
import com.sparta.moa.post.dto.PostResponse;
import com.sparta.moa.post.dto.PostSearchCondition;
import com.sparta.moa.post.dto.PostUpdateRequest;
import com.sparta.moa.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    // Controller -> Service
    private final PostService postService;
    private final PostLikeService postLikeService;

    // 게시글 등록 API : POST /api/posts
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        // service 호출 실제 비즈니스 작업을 service에 위임한다.
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        HttpStatus.CREATED,
                        postService.create(memberDetails.getMemberId(), request)
                )
        );
    }

    // 게시글 목록 페이징 조회 API : GET /api/posts
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> findAll(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        PageResponse<PostResponse> page = postService.findAll(pageable);       // 캐시됨

        Long memberId = (memberDetails == null) ? null : memberDetails.getMemberId();
        List<Long> postIds = page.content().stream().map(PostResponse::id).toList();
        Set<Long> liked = postLikeService.findLikedPostIds(memberId, postIds);  // 캐시 밖

        PageResponse<PostResponse> marked = new PageResponse<>(
                page.content().stream()
                        .map(p -> p.withLiked(liked.contains(p.id())))
                        .toList(),
                page.totalElements(), page.totalPages(), page.number(), page.size());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, marked));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> findOne(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        PostResponse post = postService.findOne(postId);

        Long memberId = (memberDetails == null) ? null : memberDetails.getMemberId();
        Set<Long> liked = postLikeService.findLikedPostIds(memberId, List.of(postId));

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                post.withLiked(liked.contains(postId))));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> search(
            @ModelAttribute PostSearchCondition condition,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, postService.search(condition, pageable)));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        postService.update(postId, memberDetails.getMemberId(), request)
                )
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId,
                                       @AuthenticationPrincipal MemberDetails memberDetails) {
        postService.delete(postId, memberDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

}
