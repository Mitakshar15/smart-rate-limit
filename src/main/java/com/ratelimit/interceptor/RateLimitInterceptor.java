package com.ratelimit.interceptor;

import com.ratelimit.annotation.RateLimit;
import com.ratelimit.config.KeyGenerator;
import com.ratelimit.service.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final RateLimiter rateLimiter;
    private final KeyGenerator keyGenerator;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler) {
        if (handler instanceof HandlerMethod method) {
            RateLimit rateLimit = method.getMethodAnnotation(RateLimit.class);
            if (rateLimit != null) {
                String endpoint = request.getRequestURI();
                String key = keyGenerator.generate(request, rateLimit.scope(), endpoint);
                log.debug("Checking rate limit for key: {}", key);
                boolean allowed = rateLimiter.allowRequest(key, rateLimit.requests(), rateLimit.durationMinutes());
                if (!allowed) {
                    log.warn("Rate limit exceeded for key: {}", key);
                    response.setStatus(429);
                    return false;
                }
            }
        }
        return true;
    }
}