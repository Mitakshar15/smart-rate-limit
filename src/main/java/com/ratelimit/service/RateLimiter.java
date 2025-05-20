package com.ratelimit.service;


public interface RateLimiter {
    boolean allowRequest(String key, int requests, int durationMinutes);
}