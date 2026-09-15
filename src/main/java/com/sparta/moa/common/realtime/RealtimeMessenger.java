package com.sparta.moa.common.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 실시간으로 무엇을 보내는 유일한 자리입니다.
 * 이 클래스 말고 어디서도 convertAndSendToUser 를 직접 부르지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeMessenger {

    public static final String CHANNEL = "realtime";

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public void send(String targetEmail, String destination, Object payload) {
        try {
            String json = jsonMapper.writeValueAsString(
                    new RealtimeMessage(targetEmail, destination, payload));

            redisTemplate.convertAndSend(CHANNEL, json);

        } catch (Exception e) {
            // 실시간 전달은 부가 기능이다. 실패해도 원래 작업은 성공해야 한다
            log.warn("실시간 전송 실패. target={}", targetEmail, e);
        }
    }
}