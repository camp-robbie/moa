package com.sparta.moa.post.controller;

import com.sparta.moa.common.dto.ApiResponse;
import com.sparta.moa.common.dto.PageResponse;
import com.sparta.moa.post.dto.PostCreateRequest;
import com.sparta.moa.post.dto.PostResponse;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    // Controller -> Service
    private final PostService postService;

    // 게시글 등록 API : POST /api/posts
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(@Valid @RequestBody PostCreateRequest request) {
        // service 호출 실제 비즈니스 작업을 service에 위임한다.
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        HttpStatus.CREATED,
                        postService.create(request)
                )
        );
    }

    // 게시글 목록 페이징 조회 API : GET /api/posts
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> findAll(
           @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        PageResponse.from(postService.findAll(pageable))
                )
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> findOne(@PathVariable Long postId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        postService.findOne(postId)
                )
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK,
                        postService.update(postId, request)
                )
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId) {
        postService.delete(postId);
        return ResponseEntity.noContent().build();
    }

}
