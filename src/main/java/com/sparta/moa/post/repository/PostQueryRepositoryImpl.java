package com.sparta.moa.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.moa.post.dto.PostSearchCondition;
import com.sparta.moa.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.sparta.moa.post.entity.QPost.post;
import static com.sparta.moa.member.entity.QMember.member;

@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {
    // QueryDSL
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> search(PostSearchCondition condition, Pageable pageable) {

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()
                .where(
                        titleContains(condition.title()),
                        nicknameEq(condition.nickname()),
                        createdBetween(condition.from(), condition.to())
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .join(post.member, member)
                .where(
                        titleContains(condition.title()),
                        nicknameEq(condition.nickname()),
                        createdBetween(condition.from(), condition.to())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression titleContains(String title) {
        return (title == null || title.isBlank())
                ? null
                : post.title.contains(title);
    }

    private BooleanExpression nicknameEq(String nickname) {
        return (nickname == null || nickname.isBlank())
                ? null
                : member.nickname.eq(nickname);   // 조인해 둔 별칭을 그대로 쓴다
    }

    private BooleanExpression createdBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return null;
        }
        if (from == null) {
            return post.createdAt.lt(to.plusDays(1).atStartOfDay());
        }
        if (to == null) {
            return post.createdAt.goe(from.atStartOfDay());
        }

        // goe >=, lt <
        // 시작일 >=, 종료일 < (8-31 x, 9-1)
        // to.atTime(LocalTime.MAX) => 나노초 00:00:00.999999999999 => MYSQL 반올림
        return post.createdAt.goe(from.atStartOfDay())
                .and(post.createdAt.lt(to.plusDays(1).atStartOfDay()));
    }

}
