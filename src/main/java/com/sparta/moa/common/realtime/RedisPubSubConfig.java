package com.sparta.moa.common.realtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    // 백그라운드에서 Redis 에 붙어 SUBSCRIBE 를 걸어 둔다.
    // 캐시와 달리 Pub/Sub 은 계속 듣고 있어야 한다
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RealtimeSubscriber realtimeSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                realtimeSubscriber, ChannelTopic.of(RealtimeMessenger.CHANNEL));

        return container;
    }
}