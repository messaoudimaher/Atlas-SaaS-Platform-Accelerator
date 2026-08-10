package com.atlas.shared.security.rbac;

public enum Role {
    OWNER(100),
    ADMIN(80),
    MEMBER(40),
    VIEWER(20);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean includes(Role requiredRole) {
        return this.level >= requiredRole.level;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return VIEWER;
        }
        try {
            return Role.valueOf(roleStr.trim().toUpperCase().replace("ROLE_", ""));
        } catch (IllegalArgumentException e) {
            return VIEWER;
        }
    }
}
