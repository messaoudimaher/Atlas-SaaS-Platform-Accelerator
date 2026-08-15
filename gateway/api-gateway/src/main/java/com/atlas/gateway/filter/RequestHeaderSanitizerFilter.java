package com.atlas.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RequestHeaderSanitizerFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestHeaderSanitizerFilter.class);

    private static final List<String> SENSITIVE_HEADERS = List.of(
            "X-User-Id",
            "X-User-Roles",
            "X-Gateway-Token"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Check if any sensitive headers were injected by the client
        boolean hasSensitiveHeaders = SENSITIVE_HEADERS.stream()
                .anyMatch(header -> request.getHeaders().containsKey(header));

        if (hasSensitiveHeaders) {
            log.warn("Ingress threat detected: client attempted to inject sensitive security headers. Sanitizing request.");

            ServerHttpRequest.Builder requestBuilder = request.mutate();
            for (String header : SENSITIVE_HEADERS) {
                requestBuilder.headers(httpHeaders -> httpHeaders.remove(header));
            }

            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Must execute at absolute HIGHEST_PRECEDENCE to sanitize headers before any gateway filters run
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
