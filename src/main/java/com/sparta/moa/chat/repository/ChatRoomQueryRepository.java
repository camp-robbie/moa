package com.sparta.moa.chat.repository;

import java.util.List;

public interface ChatRoomQueryRepository {

    List<ChatRoomWithLast> findMyRooms(Long memberId);
}