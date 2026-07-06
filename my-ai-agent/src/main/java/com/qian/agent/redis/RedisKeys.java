package com.qian.agent.redis;

public final class RedisKeys {

    private RedisKeys() {
    }

    public static String sessionCache(Long sessionId) {
        return "agent:session:" + sessionId;
    }

    public static String rateLimit(String clientId) {
        return "agent:ratelimit:" + clientId;
    }
}
