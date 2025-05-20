package com.ratelimit.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryRateLimiter implements RateLimiter {

    private static class Bucket {
        int count;
        long expiresAt;

        Bucket(int count, long expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }
    }

    private final ConcurrentMap<String, Bucket> storage = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String key, int requests, int durationMinutes) {
        long now = System.currentTimeMillis();
        long windowMillis = durationMinutes * 60 * 1000L;

        // Capture the result of compute()
        Bucket bucket = storage.compute(key, (k, existingBucket) -> {
            if (existingBucket == null || now >= existingBucket.expiresAt) {
                return new Bucket(1, now + windowMillis);
            } else {
                existingBucket.count++;
                return existingBucket;
            }
        });

        return bucket.count <= requests;  // Use the computed bucket directly
    }
}