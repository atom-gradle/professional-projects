package com.qian.agent.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SessionCacheRepository {

    private final StringRedisTemplate redisTemplate;

    @Value("${agent.session.cache-ttl-minutes:60}")
    private long cacheTtlMinutes;

    public void cacheLastReply(Long sessionId, String reply) {
        redisTemplate.opsForValue().set(
                RedisKeys.sessionCache(sessionId),
                reply,
                Duration.ofMinutes(cacheTtlMinutes)
        );
    }

    public Optional<String> getLastReply(Long sessionId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(RedisKeys.sessionCache(sessionId)));
    }

    public void evict(Long sessionId) {
        redisTemplate.delete(RedisKeys.sessionCache(sessionId));
    }
}
