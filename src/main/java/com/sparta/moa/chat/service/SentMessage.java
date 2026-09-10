package com.sparta.moa.chat.service;

import com.sparta.moa.chat.dto.ChatMessagePush;

/**
 * 저장 트랜잭션 안에서 꺼내 두고, 밖에서 밀 때 씁니다.
 * 밖에서는 프록시를 열 수 없으므로 Entity 를 그대로 들고 나가지 않습니다.
 */
public record SentMessage(
        String senderEmail,
        String partnerEmail,
        ChatMessagePush push
) {
}