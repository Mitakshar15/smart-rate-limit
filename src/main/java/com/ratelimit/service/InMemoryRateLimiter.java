package com.ratelimit.service;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InMemoryRateLimiter implements RateLimiter {

    private final Map<String, RateLimit> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int requests, int durationMinutes) {
        RateLimit rateLimit = limiters.compute(key, (k, v) -> {
            if (v == null || v.isExpired()) {
                return new RateLimit(requests, durationMinutes);
            }
            return v;
        });

        return rateLimit.tryAcquire();
    }

    private static class RateLimit {
        private final int maxRequests;
        private final AtomicInteger currentRequests;
        private final long resetTimeMillis;

        public RateLimit(int requests, int durationMinutes) {
            this.maxRequests = requests;
            this.currentRequests = new AtomicInteger(0);
            this.resetTimeMillis = System.currentTimeMillis() + ((long) durationMinutes * 60 * 1000);
        }

        public boolean tryAcquire() {
            if (isExpired()) {
                currentRequests.set(0);
                return true;
            }

            int count = currentRequests.incrementAndGet();
            return count <= maxRequests;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > resetTimeMillis;
        }
    }
}