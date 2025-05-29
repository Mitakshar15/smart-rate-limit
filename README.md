# Smart Rate Limit

A flexible and efficient rate limiting library for Spring Boot applications.

## Overview

Smart Rate Limit is a Spring Boot library that provides annotation-based rate limiting capabilities for your REST APIs. It supports multiple storage backends and configurable rate limiting strategies to protect your applications from excessive use.

## Features

- **Simple Annotation-Based Configuration**: Apply rate limiting with a simple `@RateLimit` annotation
- **Multiple Scope Options**: Limit by IP address, user, or endpoint
- **Flexible Storage Options**: 
  - Redis-based implementation for distributed systems
  - In-memory implementation for simpler deployments
- **Customizable Rate Limiting**: Configure requests per time period at both global and endpoint levels
- **Spring Boot Auto-configuration**: Minimal setup required with sensible defaults

## Requirements

- Java 17 or higher
- Spring Boot 3.x
- Redis (optional, for distributed rate limiting)

## Installation

Add the following dependency to your Maven `pom.xml`:

```xml
<dependency>
  <groupId>com.github.Mitakshar15</groupId>
  <artifactId>smart-rate-limit</artifactId>
  <version>1.0.1</version>
</dependency>
```

## Quick Start

1. Add the dependency to your project
2. Apply the `@RateLimit` annotation to your controller methods:

```java
@RestController
public class ApiController {

    @GetMapping("/api/resource")
    @RateLimit(requests = 50, durationMinutes = 1, scope = RateLimitScope.IP)
    public ResponseEntity<String> getResource() {
        return ResponseEntity.ok("Resource data");
    }
}
```

## Configuration

Configure the rate limiter in your `application.properties` or `application.yml`:

```properties
# Rate limiter type: "redis" or "in-memory"
rate-limiter.type=redis

# Default limits (applied when not specified in annotations)
rate-limiter.default-requests=100
rate-limiter.default-duration=1
```

### Redis Configuration

When using Redis as the storage backend, ensure your Spring Boot application has Redis configured:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## Advanced Usage

### Custom Scoping

Choose from different scoping options to control how rate limits are applied:

```java
// Limit by client IP address
@RateLimit(scope = RateLimitScope.IP)

// Limit by authenticated user
@RateLimit(scope = RateLimitScope.USER)

// Limit by endpoint (regardless of user or IP)
@RateLimit(scope = RateLimitScope.ENDPOINT)
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
