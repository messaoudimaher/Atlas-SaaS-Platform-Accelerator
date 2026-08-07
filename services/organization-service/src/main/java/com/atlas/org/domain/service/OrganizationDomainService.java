package com.atlas.org.domain.service;

import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;
import com.atlas.org.domain.port.in.OrganizationUseCase;
import com.atlas.org.domain.port.out.OrganizationRepositoryPort;

import java.util.UUID;

public class OrganizationDomainService implements OrganizationUseCase {

    private final OrganizationRepositoryPort repositoryPort;

    public OrganizationDomainService(OrganizationRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Organization registerOrganization(String name) {
        Organization organization = Organization.createNew(name);
        
        // Trigger PostgreSQL tenant schema instantiation dynamically
        repositoryPort.createTenantSchema(organization.getId());
        
        return repositoryPort.save(organization);
    }

    @Override
    public Workspace createWorkspace(UUID orgId, String workspaceName) {
        Workspace workspace = Workspace.createNew(orgId, workspaceName);
        return repositoryPort.saveWorkspace(workspace);
    }

    @Override
    public Organization getOrganization(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found for ID: " + id));
    }
}
