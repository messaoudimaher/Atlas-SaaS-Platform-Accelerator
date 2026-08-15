package com.atlas.org.adapter.in.rest;

import com.atlas.org.adapter.out.metrics.OrgMetricsService;
import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;
import com.atlas.org.domain.port.in.OrganizationUseCase;
import com.atlas.shared.kernel.model.ApiResponse;
import com.atlas.shared.security.context.TenantContext;
import com.atlas.shared.security.rbac.RequireRole;
import com.atlas.shared.security.rbac.Role;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OrganizationController {

    private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationUseCase organizationUseCase;
    private final OrgMetricsService metricsService;

    public OrganizationController(OrganizationUseCase organizationUseCase, OrgMetricsService metricsService) {
        this.organizationUseCase = organizationUseCase;
        this.metricsService = metricsService;
    }

    @PostMapping("/organizations")
    public ResponseEntity<ApiResponse<Organization>> registerOrganization(
            @Valid @RequestBody RegisterOrganizationRequest request) {
        Organization organization = organizationUseCase.registerOrganization(request.name());
        metricsService.incrementOrgCreated();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(organization));
    }

    @PostMapping("/workspaces")
    @RequireRole(Role.ADMIN)
    public ResponseEntity<ApiResponse<Workspace>> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request) {
        
        // Retrieve tenant ID context set by the Gateway X-Tenant-Id header
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null || currentTenant.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Tenant ID context header is missing", "MISSING_TENANT_CONTEXT"));
        }

        UUID orgId = UUID.fromString(currentTenant);
        Workspace workspace = organizationUseCase.createWorkspace(orgId, request.name());
        metricsService.incrementWorkspaceCreated();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(workspace));
    }

    @GetMapping("/organizations/{id}")
    @RequireRole(Role.MEMBER)
    @CircuitBreaker(name = "organizationService", fallbackMethod = "fallbackGetOrganization")
    @Retry(name = "organizationService")
    public ResponseEntity<ApiResponse<Organization>> getOrganization(@PathVariable UUID id) {
        Organization organization = organizationUseCase.getOrganization(id);
        return ResponseEntity.ok(ApiResponse.success(organization));
    }

    public ResponseEntity<ApiResponse<Organization>> fallbackGetOrganization(UUID id, Throwable t) {
        log.error("Circuit breaker active or service failure for getOrganization [{}]: {}", id, t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Organization service is temporarily degraded. Please try again later.", "SERVICE_DEGRADED"));
    }

    public record RegisterOrganizationRequest(@NotBlank(message = "Name is required") String name) {}
    public record CreateWorkspaceRequest(@NotBlank(message = "Name is required") String name) {}
}
