package com.sparta.moa.comment.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.moa.comment.dto.CommentResponse;
import com.sparta.moa.comment.entity.Comment;
import com.sparta.moa.common.dto.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.sparta.moa.comment.entity.QComment.comment;
import static com.sparta.moa.member.entity.QMember.member;

@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorResponse<CommentResponse> findByCursor(Long postId, Long cursor, int size) {

        List<Comment> comments = queryFactory
                .selectFrom(comment)
                .join(comment.member, member).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        cursorGt(cursor)
                )
                .orderBy(comment.id.asc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = comments.size() > size;
        if (hasNext) {
            comments = comments.subList(0, size);
        }

        Long nextCursor = hasNext
                ? comments.get(comments.size() - 1).getId()
                : null;

        return new CursorResponse<>(
                comments.stream().map(CommentResponse::from).toList(),
                nextCursor,
                hasNext
        );
    }

    // 커서가 없으면 처음부터. null 이면 조건이 무시된다
    private BooleanExpression cursorGt(Long cursor) {
        return cursor == null ? null : comment.id.gt(cursor);
    }
}
