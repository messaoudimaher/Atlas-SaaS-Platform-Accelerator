package com.atlas.org.adapter.in.rest;

import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;
import com.atlas.org.domain.port.in.OrganizationUseCase;
import com.atlas.shared.kernel.model.ApiResponse;
import com.atlas.shared.security.context.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OrganizationController {

    private final OrganizationUseCase organizationUseCase;

    public OrganizationController(OrganizationUseCase organizationUseCase) {
        this.organizationUseCase = organizationUseCase;
    }

    @PostMapping("/organizations")
    public ResponseEntity<ApiResponse<Organization>> registerOrganization(
            @Valid @RequestBody RegisterOrganizationRequest request) {
        Organization organization = organizationUseCase.registerOrganization(request.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(organization));
    }

    @PostMapping("/workspaces")
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(workspace));
    }

    @GetMapping("/organizations/{id}")
    public ResponseEntity<ApiResponse<Organization>> getOrganization(@PathVariable UUID id) {
        Organization organization = organizationUseCase.getOrganization(id);
        return ResponseEntity.ok(ApiResponse.success(organization));
    }

    public record RegisterOrganizationRequest(@NotBlank(message = "Name is required") String name) {}
    public record CreateWorkspaceRequest(@NotBlank(message = "Name is required") String name) {}
}
