package com.atlas.shared.kernel.exception;

import org.springframework.http.HttpStatus;

public class TenantAccessDeniedException extends BaseException {
    public TenantAccessDeniedException(String message) {
        super(message, "TENANT_ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }
}
