package com.sparta.moa.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.moa.chat.entity.ChatMessage;
import com.sparta.moa.chat.entity.ChatRoom;
import com.sparta.moa.member.entity.QMember;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sparta.moa.chat.entity.QChatMessage.chatMessage;
import static com.sparta.moa.chat.entity.QChatRoom.chatRoom;

@RequiredArgsConstructor
public class ChatRoomQueryRepositoryImpl implements ChatRoomQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatRoomWithLast> findMyRooms(Long memberId) {

        // 같은 Member 를 두 번 조인하므로 별칭을 따로 준다
        QMember low = new QMember("low");
        QMember high = new QMember("high");

        // ① 내가 속한 방. 양쪽 회원을 같이 가져온다
        List<ChatRoom> rooms = queryFactory
                .selectFrom(chatRoom)
                .join(chatRoom.memberLow, low).fetchJoin()
                .join(chatRoom.memberHigh, high).fetchJoin()
                .where(low.id.eq(memberId).or(high.id.eq(memberId)))
                .fetch();

        if (rooms.isEmpty()) {
            return List.of();
        }

        List<Long> roomIds = rooms.stream().map(ChatRoom::getId).toList();

        // ② 방마다 마지막 메시지의 id 를 한 번에
        List<Long> lastIds = queryFactory
                .select(chatMessage.id.max())
                .from(chatMessage)
                .where(chatMessage.room.id.in(roomIds))
                .groupBy(chatMessage.room.id)
                .fetch();

        // ③ 그 메시지들을 한 번에
        Map<Long, ChatMessage> lastByRoom = lastIds.isEmpty()
                ? Map.of()
                : queryFactory
                  .selectFrom(chatMessage)
                  .where(chatMessage.id.in(lastIds))
                  .fetch()
                  .stream()
                  .collect(Collectors.toMap(m -> m.getRoom().getId(), m -> m));

        return rooms.stream()
                .map(r -> new ChatRoomWithLast(r, lastByRoom.get(r.getId())))
                .sorted(Comparator.comparing(
                        (ChatRoomWithLast r) -> lastAt(r)).reversed())
                .toList();
    }

    // 한 마디도 안 한 방은 방이 생긴 시각을 쓴다
    private LocalDateTime lastAt(ChatRoomWithLast r) {
        return r.last() == null ? r.room().getCreatedAt() : r.last().getCreatedAt();
    }
}