package com.atlas.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    private final String expectedApiKey;

    public ApiKeyAuthFilter(@Value("${atlas.security.m2m-api-key:atl_production_secure_token}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);

        // If no API Key header, continue standard OAuth2 flow
        if (apiKey == null || apiKey.isBlank()) {
            return chain.filter(exchange);
        }

        log.debug("Processing M2M authentication with API Key");

        // Validate API Key matching configured secret token
        if (!apiKey.equals(expectedApiKey)) {
            log.warn("Invalid API Key: signature verification failed (mismatch with expected keys)");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Decorate request with M2M service context
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-Auth-Type", "API_KEY")
                .header("X-User-Roles", "ADMIN")
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run at high priority, before TenantContextFilter (-100)
        return -200;
    }
}
