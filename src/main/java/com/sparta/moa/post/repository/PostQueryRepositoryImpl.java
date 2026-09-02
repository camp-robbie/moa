package com.sparta.moa.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.moa.post.dto.PostSearchCondition;
import com.sparta.moa.post.dto.PostWithCount;
import com.sparta.moa.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sparta.moa.post.entity.QPost.post;
import static com.sparta.moa.member.entity.QMember.member;
import static com.sparta.moa.comment.entity.QComment.comment;

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

    @Override
    public Page<PostWithCount> findAllWithCommentCount(Pageable pageable) {

        // ① 글 10개 — 작성자를 같이 가져온다
        List<Post> posts = queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // ② 그 10개의 댓글 수를 한 번에
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        Map<Long, Long> countMap = postIds.isEmpty()
                ? Map.of()
                : queryFactory
                  .select(comment.post.id, comment.count())
                  .from(comment)
                  .where(comment.post.id.in(postIds))
                  .groupBy(comment.post.id)
                  .fetch()
                  .stream()
                  .collect(Collectors.toMap(
                          t -> t.get(comment.post.id),
                          t -> t.get(comment.count())
                  ));

        List<PostWithCount> content = posts.stream()
                .map(p -> new PostWithCount(p, countMap.getOrDefault(p.getId(), 0L)))
                .toList();

        // ③ 전체 개수
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post);

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
