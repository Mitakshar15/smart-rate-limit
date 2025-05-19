package com.ratelimit.interceptor;

import com.ratelimit.service.InMemoryRateLimiter;
import com.ratelimit.service.RateLimiter;
import com.ratelimit.service.RedisRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RateLimiterFactory {

    @Bean
    public RateLimiter rateLimiter(
            ApplicationContext context,
            @Value("${rate-limit.storage}") String storageType) {

        if ("redis".equals(storageType) && context.getBeanNamesForType(RedisTemplate.class).length > 0) {
            RedisTemplate<String, String> redisTemplate = context.getBean(RedisTemplate.class);
            return new RedisRateLimiter(redisTemplate);
        }

        return new InMemoryRateLimiter();
    }
}