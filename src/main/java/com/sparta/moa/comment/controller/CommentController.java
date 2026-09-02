package com.sparta.moa.comment.controller;

import com.sparta.moa.comment.dto.CommentCreateRequest;
import com.sparta.moa.comment.dto.CommentResponse;
import com.sparta.moa.comment.dto.CommentUpdateRequest;
import com.sparta.moa.comment.service.CommentService;
import com.sparta.moa.common.dto.ApiResponse;
import com.sparta.moa.common.dto.CursorResponse;
import com.sparta.moa.common.security.MemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        CommentResponse response =
                commentService.create(postId, memberDetails.getMemberId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CursorResponse<CommentResponse>>> findByPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK, commentService.findByPost(postId, cursor, size)));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        CommentResponse response = commentService.update(
                postId, commentId, memberDetails.getMemberId(), request);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        commentService.delete(postId, commentId, memberDetails.getMemberId());

        return ResponseEntity.noContent().build();   // 204는 본문을 보내지 않는다
    }
}