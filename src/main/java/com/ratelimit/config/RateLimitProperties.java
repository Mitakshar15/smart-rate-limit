package com.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimitProperties {
    private String type = "redis";
    private int defaultRequests = 100;
    private int defaultDuration = 1;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDefaultRequests() {
        return defaultRequests;
    }

    public void setDefaultRequests(int defaultRequests) {
        this.defaultRequests = defaultRequests;
    }

    public int getDefaultDuration() {
        return defaultDuration;
    }

    public void setDefaultDuration(int defaultDuration) {
        this.defaultDuration = defaultDuration;
    }
}