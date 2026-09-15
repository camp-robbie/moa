package com.sparta.moa.notification.service;

import com.sparta.moa.comment.entity.Comment;
import com.sparta.moa.common.realtime.RealtimeMessenger;
import com.sparta.moa.member.entity.Member;
import com.sparta.moa.notification.dto.NotificationResponse;
import com.sparta.moa.post.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RealtimeMessenger realtimeMessenger;

    public void notifyNewComment(Post post, Comment comment) {

        Member receiver = post.getMember();      // 알림을 받을 사람 = 글쓴이
        Member writer = comment.getMember();     // 댓글을 쓴 사람

        // 내 글에 내가 단 댓글. 나에게 알릴 이유가 없다
        if (receiver.getId().equals(writer.getId())) {
            return;
        }

        realtimeMessenger.send(
                receiver.getEmail(),
                "/queue/notifications",
                new NotificationResponse(
                        writer.getNickname(), "댓글을 남겼습니다", post.getId()));

    }
}