package com.atlas.shared.security.filter;

import com.atlas.shared.security.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TenantContextInterceptorTest {

    private TenantContextInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new TenantContextInterceptor();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void preHandle_setsTenantAndUserInContext() throws Exception {
        when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-123");
        when(request.getHeader("X-User-Id")).thenReturn("user-456");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals("tenant-123", TenantContext.getCurrentTenant());
        assertEquals("user-456", TenantContext.getCurrentUser());
    }

    @Test
    void preHandle_doesNotOverwriteExistingContext_ifHeadersMissing() throws Exception {
        TenantContext.setCurrentTenant("original-tenant");
        when(request.getHeader("X-Tenant-Id")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals("original-tenant", TenantContext.getCurrentTenant());
    }

    @Test
    void afterCompletion_cleansContext() throws Exception {
        TenantContext.setCurrentTenant("tenant-123");
        TenantContext.setCurrentUser("user-456");

        interceptor.afterCompletion(request, response, new Object(), null);

        assertNull(TenantContext.getCurrentTenant());
        assertNull(TenantContext.getCurrentUser());
    }
}
