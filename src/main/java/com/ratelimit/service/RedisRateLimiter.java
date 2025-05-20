package com.ratelimit.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.util.Collections;
import java.util.List;

public class RedisRateLimiter implements RateLimiter {

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    public RedisRateLimiter(RedisConnectionFactory connectionFactory) {
        this.redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();

        this.rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setScriptText(
                "local current = redis.call('INCR', KEYS[1])\n" +
                        "if current == 1 then\n" +
                        "    redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
                        "end\n" +
                        "return current > tonumber(ARGV[2]) and 0 or 1"
        );
        rateLimitScript.setResultType(Long.class);
    }

    @Override
    public boolean allowRequest(String key, int requests, int durationMinutes) {
        List<String> keys = Collections.singletonList(key);
        String expiration = String.valueOf(durationMinutes * 60);
        String maxRequests = String.valueOf(requests);
        Long result = redisTemplate.execute(rateLimitScript, keys, expiration, maxRequests);
        return result == 1;
    }
}