package com.ratelimit.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimitProperties {
    private String type = "redis";
    private int defaultRequests = 100;
    private int defaultDuration = 1;
}