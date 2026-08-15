package com.atlas.shared.security.filter;

import com.atlas.shared.kernel.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class GatewaySecurityFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(GatewaySecurityFilter.class);
    private static final String GATEWAY_TOKEN_HEADER = "X-Gateway-Token";

    private final boolean enabled;
    private final String secret;
    private final ObjectMapper objectMapper;

    public GatewaySecurityFilter(
            @Value("${atlas.security.gateway-token-check.enabled:false}") boolean enabled,
            @Value("${atlas.security.gateway-token-check.secret:}") String secret,
            ObjectMapper objectMapper) {
        this.enabled = enabled;
        this.secret = secret;
        this.objectMapper = objectMapper;
        log.info("GatewaySecurityFilter initialized (enabled={}, secretLength={})", enabled, secret != null ? secret.length() : 0);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String token = httpRequest.getHeader(GATEWAY_TOKEN_HEADER);

            if (token == null || token.isBlank() || !token.equals(secret)) {
                log.warn("Unauthorized direct request blocked: missing or invalid [{}] header (clientIp={})", 
                        GATEWAY_TOKEN_HEADER, httpRequest.getRemoteAddr());
                
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                ApiResponse<Void> apiResponse = ApiResponse.error(
                        "Unauthorized direct port access detected. Access must route via API Gateway.", 
                        "UNAUTHORIZED_DIRECT_ACCESS"
                );
                
                objectMapper.writeValue(httpResponse.getWriter(), apiResponse);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
