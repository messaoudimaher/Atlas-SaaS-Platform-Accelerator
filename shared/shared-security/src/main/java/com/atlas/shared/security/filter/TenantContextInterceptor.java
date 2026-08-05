package com.atlas.shared.security.filter;

import com.atlas.shared.security.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class TenantContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenantId = request.getHeader("X-Tenant-Id");
        String userId = request.getHeader("X-User-Id");

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }

        if (userId != null) {
            TenantContext.setCurrentUser(userId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Enforce cleaning ThreadLocal to prevent data leaks when thread pools are recycled
        TenantContext.clear();
    }
}
