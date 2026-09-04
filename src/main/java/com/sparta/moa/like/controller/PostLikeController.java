package com.sparta.moa.like.controller;

import com.sparta.moa.common.dto.ApiResponse;
import com.sparta.moa.common.security.MemberDetails;
import com.sparta.moa.like.dto.LikeResponse;
import com.sparta.moa.like.service.PostLikeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
public class PostLikeController {

    private final PostLikeFacade postLikeFacade;      // Service 가 아니라 Facade

    @PostMapping
    public ResponseEntity<ApiResponse<LikeResponse>> like(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        LikeResponse response = postLikeFacade.like(postId, memberDetails.getMemberId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        LikeResponse response = postLikeFacade.unlike(postId, memberDetails.getMemberId());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }
}