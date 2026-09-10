package com.sparta.moa.chat.repository;

import com.sparta.moa.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long>, ChatMessageQueryRepository {
}