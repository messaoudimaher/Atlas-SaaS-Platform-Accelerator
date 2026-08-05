package com.atlas.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TenantContextFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(auth -> {
                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    Jwt jwt = jwtAuth.getToken();
                    String tenantId = jwt.getClaimAsString("tenant_id");
                    String userId = jwt.getSubject(); // OIDC standard 'sub' claim
                    
                    // Extract user roles from Keycloak access
                    Collection<String> roles = extractRoles(jwt);
                    String rolesHeader = String.join(",", roles);

                    ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
                    
                    if (tenantId != null) {
                        requestBuilder.header("X-Tenant-Id", tenantId);
                    }
                    if (userId != null) {
                        requestBuilder.header("X-User-Id", userId);
                    }
                    if (!roles.isEmpty()) {
                        requestBuilder.header("X-User-Roles", rolesHeader);
                    }

                    return exchange.mutate().request(requestBuilder.build()).build();
                }
                return exchange;
            })
            .defaultIfEmpty(exchange)
            .flatMap(chain::filter);
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public int getOrder() {
        // Run immediately after the Spring Security filter chain populates context
        return -100;
    }
}
