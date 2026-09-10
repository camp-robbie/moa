package com.sparta.moa.chat.repository;

import com.sparta.moa.chat.dto.ChatMessageResponse;
import com.sparta.moa.common.dto.CursorResponse;

public interface ChatMessageQueryRepository {

    CursorResponse<ChatMessageResponse> findByCursor(
            Long roomId, Long cursor, int size, Long memberId);
}