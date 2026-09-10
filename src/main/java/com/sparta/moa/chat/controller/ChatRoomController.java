package com.sparta.moa.chat.controller;

import com.sparta.moa.chat.dto.ChatMessageResponse;
import com.sparta.moa.chat.dto.ChatRoomCreateRequest;
import com.sparta.moa.chat.dto.ChatRoomResponse;
import com.sparta.moa.chat.service.ChatMessageService;
import com.sparta.moa.chat.service.ChatRoomService;
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
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

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

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<CursorResponse<ChatMessageResponse>>> findMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,
                chatMessageService.findByCursor(
                        roomId, memberDetails.getMemberId(), cursor, size)));
    }

}