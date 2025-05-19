package com.ratelimit.config;

import com.ratelimit.interceptor.RateLimitInterceptor;
import com.ratelimit.service.InMemoryRateLimiter;
import com.ratelimit.service.RateLimiter;
import com.ratelimit.service.RedisRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitAutoConfiguration {

    @Configuration
    static class RateLimiterConfiguration {

        @Bean
        @ConditionalOnProperty(name = "rate-limit.storage", havingValue = "redis")
        @ConditionalOnClass(RedisTemplate.class)
        public RateLimiter redisRateLimiter(RedisTemplate<String, String> redisTemplate) {
            return new RedisRateLimiter(redisTemplate);
        }

        @Bean
        @ConditionalOnMissingBean(RateLimiter.class)
        public RateLimiter inMemoryRateLimiter() {
            return new InMemoryRateLimiter();
        }
    }

    @Configuration
    static class RateLimitInterceptorConfiguration implements WebMvcConfigurer {

        private final RateLimiter rateLimiter;

        @Autowired
        public RateLimitInterceptorConfiguration(RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }

        @Bean
        public RateLimitInterceptor rateLimitInterceptor() {
            return new RateLimitInterceptor(rateLimiter);
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(rateLimitInterceptor());
        }
    }
}