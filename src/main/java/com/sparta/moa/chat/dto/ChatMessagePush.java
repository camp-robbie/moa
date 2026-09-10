package com.sparta.moa.chat.dto;

public record ChatMessagePush(
        Long roomId,
        String sender
) {
}