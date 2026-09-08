package com.sparta.moa.notification.dto;

public record NotificationResponse(
        String who,        // 댓글을 쓴 사람의 닉네임
        String text,       // 화면에 "{who}님이 {text}" 로 표시된다
        Long postId        // 눌렀을 때 이동할 글
) {
}