package com.atlas.shared.observability.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ObservabilityMdcFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityMdcFilter.class);

    public static final String MDC_TENANT_ID = "tenantId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_CORRELATION_ID = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            try {
                String tenantId = httpRequest.getHeader("X-Tenant-Id");
                String userId = httpRequest.getHeader("X-User-Id");
                String correlationId = httpRequest.getHeader("X-Correlation-Id");

                if (correlationId == null || correlationId.isBlank()) {
                    correlationId = UUID.randomUUID().toString();
                }

                if (tenantId != null && !tenantId.isBlank()) {
                    MDC.put(MDC_TENANT_ID, tenantId);
                }
                if (userId != null && !userId.isBlank()) {
                    MDC.put(MDC_USER_ID, userId);
                }
                MDC.put(MDC_CORRELATION_ID, correlationId);

                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
