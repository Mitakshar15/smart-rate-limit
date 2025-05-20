package com.ratelimit.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.RedisSystemException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisRateLimiter implements RateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisRateLimiter.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    public RedisRateLimiter(RedisConnectionFactory connectionFactory) {
        this.redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setStringSerializer(new StringRedisSerializer());

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
        try {
            List<String> keys = Collections.singletonList(key);
            String expiration = String.valueOf(durationMinutes * 60);
            String maxRequests = String.valueOf(requests);
            
            logger.debug("Executing rate limit check for key: {}, max requests: {}, duration: {} minutes", 
                         key, requests, durationMinutes);
            
            Long result = redisTemplate.execute(rateLimitScript, keys, expiration, maxRequests);

            if (result == null) {
                logger.error("Redis script execution returned null for key: {}", key);
                return true;
            }

            boolean allowed = result == 1;
            logger.debug("Rate limit result for key {}: {}", key, allowed ? "allowed" : "blocked");
            return allowed;
        } catch (RedisSystemException ex) {
            logger.error("Redis error during rate limiting for key: " + key, ex);
            return true;
        } catch (Exception ex) {
            logger.error("Unexpected error during rate limiting for key: " + key, ex);
            return true;
        }
    }
}