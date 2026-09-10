package com.sparta.moa.chat.dto;

import com.sparta.moa.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        String content,
        boolean mine,            // 보는 사람 기준
        LocalDateTime sentAt
) {
    public static ChatMessageResponse from(ChatMessage message, Long memberId) {
        return new ChatMessageResponse(
                message.getId(),
                message.getContent(),
                message.getSender().getId().equals(memberId),
                message.getCreatedAt()
        );
    }
}