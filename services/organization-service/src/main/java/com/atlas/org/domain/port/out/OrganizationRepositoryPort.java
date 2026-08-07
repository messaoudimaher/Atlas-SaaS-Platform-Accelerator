package com.atlas.org.domain.port.out;

import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepositoryPort {
    Organization save(Organization organization);
    Workspace saveWorkspace(Workspace workspace);
    Optional<Organization> findById(UUID id);
    void createTenantSchema(UUID tenantId);
}
