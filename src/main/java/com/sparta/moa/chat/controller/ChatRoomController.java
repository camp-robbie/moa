package com.sparta.moa.chat.controller;

import com.sparta.moa.chat.dto.ChatRoomCreateRequest;
import com.sparta.moa.chat.dto.ChatRoomResponse;
import com.sparta.moa.chat.service.ChatRoomService;
import com.sparta.moa.common.dto.ApiResponse;
import com.sparta.moa.common.security.MemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 이미 있으면 그 방을 준다. 그래서 201 이 아니라 200
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGet(
            @Valid @RequestBody ChatRoomCreateRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatRoomService.createOrGet(memberDetails.getMemberId(), request.partner())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> findMyRooms(
            @AuthenticationPrincipal MemberDetails memberDetails) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatRoomService.findMyRooms(memberDetails.getMemberId())));
    }
}