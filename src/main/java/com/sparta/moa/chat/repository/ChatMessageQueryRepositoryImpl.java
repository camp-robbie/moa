package com.sparta.moa.chat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.moa.chat.dto.ChatMessageResponse;
import com.sparta.moa.chat.entity.ChatMessage;
import com.sparta.moa.common.dto.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.sparta.moa.chat.entity.QChatMessage.chatMessage;
import static com.sparta.moa.member.entity.QMember.member;

@RequiredArgsConstructor
public class ChatMessageQueryRepositoryImpl implements ChatMessageQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorResponse<ChatMessageResponse> findByCursor(
            Long roomId, Long cursor, int size, Long memberId) {

        // 최신부터 가져온다. 댓글과 조건도 정렬도 반대다
        List<ChatMessage> messages = queryFactory
                .selectFrom(chatMessage)
                .join(chatMessage.sender, member).fetchJoin()
                .where(
                        chatMessage.room.id.eq(roomId),
                        cursorLt(cursor)
                )
                .orderBy(chatMessage.id.desc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = messages.size() > size;
        if (hasNext) {
            messages = messages.subList(0, size);
        }

        // 커서는 뒤집기 전 마지막 것. 이 묶음에서 가장 오래된 id 다
        Long nextCursor = hasNext
                ? messages.get(messages.size() - 1).getId()
                : null;

        // 화면은 위에서 아래로 오래된 순으로 그린다. 뒤집어서 준다
        List<ChatMessageResponse> content = messages.reversed().stream()
                .map(m -> ChatMessageResponse.from(m, memberId))
                .toList();

        return new CursorResponse<>(content, nextCursor, hasNext);
    }

    // 위로 올라가므로 lt 다
    private BooleanExpression cursorLt(Long cursor) {
        return cursor == null ? null : chatMessage.id.lt(cursor);
    }
}