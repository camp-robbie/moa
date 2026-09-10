package com.sparta.moa.chat.dto;

import com.sparta.moa.chat.entity.ChatMessage;
import com.sparta.moa.chat.entity.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long id,
        String partner,          // 나 아닌 쪽의 닉네임
        String lastMessage,      // 아직 한 마디도 안 했으면 null
        LocalDateTime lastAt
) {
    public static ChatRoomResponse from(ChatRoom room, ChatMessage last, Long memberId) {
        return new ChatRoomResponse(
                room.getId(),
                room.partnerOf(memberId).getNickname(),
                last == null ? null : last.getContent(),
                last == null ? room.getCreatedAt() : last.getCreatedAt()
        );
    }
}