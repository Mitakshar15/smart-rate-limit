package com.ratelimit.config;

import com.ratelimit.interceptor.RateLimitInterceptor;
import com.ratelimit.service.InMemoryRateLimiter;
import com.ratelimit.service.RateLimiter;
import com.ratelimit.service.RedisRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "rate-limiter.type", havingValue = "redis", matchIfMissing = true)
    public RateLimiter redisRateLimiter(RedisConnectionFactory factory) {
        return new RedisRateLimiter(factory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "rate-limiter.type", havingValue = "in-memory")
    public RateLimiter inMemoryRateLimiter() {
        return new InMemoryRateLimiter();
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator();
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(RateLimiter rateLimiter, KeyGenerator keyGenerator) {
        return new RateLimitInterceptor(rateLimiter, keyGenerator);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(RateLimitInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}