package com.sparta.moa.common.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/**
 * 채널에 무엇이 오면 불립니다.
 * 내 서버에 그 사람이 없으면 convertAndSendToUser 가 조용히 버립니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final JsonMapper jsonMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            RealtimeMessage received = jsonMapper.readValue(json, RealtimeMessage.class);

            messagingTemplate.convertAndSendToUser(
                    received.targetEmail(),
                    received.destination(),
                    received.payload());

        } catch (Exception e) {
            log.warn("실시간 수신 처리 실패", e);
        }
    }
}