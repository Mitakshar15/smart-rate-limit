package com.ratelimit.config;

import com.ratelimit.annotation.RateLimitScope;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;

public class KeyGenerator {

    public String generate(HttpServletRequest request, RateLimitScope scope, String endpoint) {
        String key = switch (scope) {
            case IP -> getClientIp(request) + ":" + endpoint;
            case USER -> getUsername(request) + ":" + endpoint;
            case ENDPOINT -> endpoint;
            default -> throw new IllegalArgumentException("Invalid scope");
        };
        System.out.println("Generated rate limit key: " + key);  // Debug log
        return key;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        // If behind a proxy, use X-Forwarded-For header
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            ip = xff.split(",")[0];
        }
        return ip != null ? ip : "unknown_ip";
    }

    private String getUsername(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return (principal != null) ? principal.getName() : "anonymous";
    }
}