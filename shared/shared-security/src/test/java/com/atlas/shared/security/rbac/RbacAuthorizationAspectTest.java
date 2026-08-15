package com.atlas.shared.security.rbac;

import com.atlas.shared.kernel.exception.TenantAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RbacAuthorizationAspectTest {

    private RbacAuthorizationAspect aspect;
    private ProceedingJoinPoint joinPoint;
    private RequireRole annotation;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        aspect = new RbacAuthorizationAspect();
        joinPoint = mock(ProceedingJoinPoint.class);
        annotation = mock(RequireRole.class);
        request = mock(HttpServletRequest.class);

        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("TestClass.testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void enforceRole_allowsRequest_whenUserHasRequiredRole() throws Throwable {
        when(request.getHeader("X-User-Roles")).thenReturn("ADMIN");
        when(annotation.value()).thenReturn(Role.ADMIN);
        when(joinPoint.proceed()).thenReturn("success");

        Object result = aspect.enforceRole(joinPoint, annotation);

        assertEquals("success", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void enforceRole_allowsRequest_whenUserHasHigherRole() throws Throwable {
        when(request.getHeader("X-User-Roles")).thenReturn("OWNER");
        when(annotation.value()).thenReturn(Role.ADMIN);
        when(joinPoint.proceed()).thenReturn("success");

        Object result = aspect.enforceRole(joinPoint, annotation);

        assertEquals("success", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void enforceRole_rejectsRequest_whenUserHasLowerRole() throws Throwable {
        when(request.getHeader("X-User-Roles")).thenReturn("VIEWER");
        when(annotation.value()).thenReturn(Role.ADMIN);

        assertThrows(TenantAccessDeniedException.class, () -> aspect.enforceRole(joinPoint, annotation));
        verify(joinPoint, never()).proceed();
    }

    @Test
    void enforceRole_rejectsRequest_whenRolesHeaderMissing() throws Throwable {
        when(request.getHeader("X-User-Roles")).thenReturn(null);
        when(annotation.value()).thenReturn(Role.MEMBER);

        assertThrows(TenantAccessDeniedException.class, () -> aspect.enforceRole(joinPoint, annotation));
        verify(joinPoint, never()).proceed();
    }
}
