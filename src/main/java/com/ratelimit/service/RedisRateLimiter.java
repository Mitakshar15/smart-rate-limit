package com.ratelimit.service;


import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnClass(RedisTemplate.class)
public class RedisRateLimiter implements RateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(String key, int requests, int durationMinutes) {
        String counterKey = "rate_limit:" + key;

        Long currentCount = redisTemplate.opsForValue().increment(counterKey);

        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(counterKey, durationMinutes, TimeUnit.MINUTES);
        }

        return currentCount != null && currentCount <= requests;
    }
}