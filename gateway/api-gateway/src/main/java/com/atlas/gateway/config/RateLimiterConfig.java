package com.atlas.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfig.class);

    @Bean
    @Primary
    public KeyResolver tenantKeyResolver() {
        return exchange -> {
            // 1. Check for Tenant ID header (authenticated requests)
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
            if (tenantId != null && !tenantId.isBlank()) {
                log.trace("Rate limiting by Tenant ID: {}", tenantId);
                return Mono.just("tenant:" + tenantId);
            }

            // 2. Check for API Key (M2M requests)
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isBlank()) {
                log.trace("Rate limiting by API Key");
                return Mono.just("apikey:" + apiKey.hashCode());
            }

            // 3. Fallback to Client IP address
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String clientIp = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "anonymous";
            log.trace("Rate limiting by Client IP: {}", clientIp);
            return Mono.just("ip:" + clientIp);
        };
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // 50 requests/sec replenish rate, burst capacity of 100 requests
        return new RedisRateLimiter(50, 100, 1);
    }
}
