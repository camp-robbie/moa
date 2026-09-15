package com.sparta.moa.common.realtime;

/**
 * 서버 사이를 오가는 봉투입니다.
 * payload 가 무엇인지는 중계하는 쪽이 알 필요가 없습니다.
 */
public record RealtimeMessage(
        String targetEmail,     // 받을 사람
        String destination,     // "/queue/notifications" 또는 "/queue/messages"
        Object payload          // 브라우저에 그대로 전달할 것
) {
}