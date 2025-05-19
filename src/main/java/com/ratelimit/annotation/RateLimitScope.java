package com.ratelimit.annotation;

/**
 * Defines the scope for rate limiting.
 */
public enum RateLimitScope {
    /**
     * Rate limit based on client IP address
     */
    IP,

    /**
     * Rate limit based on authenticated user
     */
    USER,

    /**
     * Rate limit based on the endpoint being accessed
     */
    ENDPOINT
}