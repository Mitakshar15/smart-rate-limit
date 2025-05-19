package com.ratelimit.interceptor;

import com.ratelimit.annotation.RateLimit;
import com.ratelimit.service.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.security.Principal;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;

    public RateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimitAnnotation = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimitAnnotation == null) {
            return true;
        }

        String key = generateKey(request, rateLimitAnnotation, handlerMethod);

        if (!rateLimiter.tryAcquire(key, rateLimitAnnotation.requests(),
                rateLimitAnnotation.durationMinutes())) {
            sendRateLimitExceededResponse(response);
            return false;
        }

        return true;
    }

    private String generateKey(HttpServletRequest request, RateLimit annotation,
                               HandlerMethod handlerMethod) {
        String prefix = annotation.scope().name().toLowerCase() + ":";

        switch (annotation.scope()) {
            case IP:
                return prefix + getClientIp(request);
            case USER:
                return prefix + getUserIdentifier(request);
            case ENDPOINT:
                return prefix + getEndpointIdentifier(handlerMethod);
            default:
                return prefix + getClientIp(request);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserIdentifier(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal != null ? principal.getName() : getClientIp(request);
    }

    private String getEndpointIdentifier(HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().getName() + "." + handlerMethod.getMethod().getName();
    }

    private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429); // Too Many Requests
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}");
    }
}