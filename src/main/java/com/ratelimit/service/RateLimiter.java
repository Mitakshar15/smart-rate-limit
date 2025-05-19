package com.ratelimit.service;


public interface RateLimiter {
    boolean tryAcquire(String key, int requests, int durationMinutes);
}