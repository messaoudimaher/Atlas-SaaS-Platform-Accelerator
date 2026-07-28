package com.atlas.shared.security.context;

public final class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    private TenantContext() {}

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void setCurrentUser(String userId) {
        CURRENT_USER.set(userId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
    }
}
