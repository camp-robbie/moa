package com.sparta.moa.chat.repository;

import com.sparta.moa.chat.entity.ChatMessage;
import com.sparta.moa.chat.entity.ChatRoom;

public record ChatRoomWithLast(ChatRoom room, ChatMessage last) {
}