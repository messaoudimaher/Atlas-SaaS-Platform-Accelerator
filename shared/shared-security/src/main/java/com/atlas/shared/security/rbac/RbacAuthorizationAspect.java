package com.atlas.shared.security.rbac;

import com.atlas.shared.kernel.exception.TenantAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class RbacAuthorizationAspect {

    private static final Logger log = LoggerFactory.getLogger(RbacAuthorizationAspect.class);
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Around("@annotation(requireRole)")
    public Object enforceRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String rolesHeader = request.getHeader(USER_ROLES_HEADER);

        if (rolesHeader == null || rolesHeader.isBlank()) {
            log.warn("Access denied: missing [{}] header for secured method [{}]",
                    USER_ROLES_HEADER, joinPoint.getSignature().toShortString());
            throw new TenantAccessDeniedException("Authentication context missing: no user roles provided");
        }

        List<Role> userRoles = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .map(Role::fromString)
                .toList();

        Role requiredRole = requireRole.value();
        boolean isAuthorized = userRoles.stream().anyMatch(role -> role.includes(requiredRole));

        if (!isAuthorized) {
            log.warn("Access denied for user with roles {} on method [{}]. Required role: {}",
                    userRoles, joinPoint.getSignature().toShortString(), requiredRole);
            throw new TenantAccessDeniedException("Insufficient permissions: requires minimum role " + requiredRole);
        }

        log.trace("RBAC authorization granted for role [{}] on method [{}]", requiredRole, joinPoint.getSignature().toShortString());
        return joinPoint.proceed();
    }
}
